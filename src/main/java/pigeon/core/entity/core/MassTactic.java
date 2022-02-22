package pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigeon.core.dao.MassTacticDAO;
import pigeon.core.dao.MessageMassDAO;
import pigeon.core.data.MassTacticDO;
import pigeon.core.data.MessageMassDO;
import pigeon.core.entity.core.mass.PartitionMessageMass;
import pigeon.core.repo.MessageMassRepo;
import pigeon.core.repo.MessageTemplateRepo;
import pigeon.core.service.TransactionWrapper;
import pigeon.core.valueobj.MessageInfo;
import pigeon.core.valueobj.Source;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 消息群发策略
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MassTactic extends Entity.Base<Long> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    protected MassTacticDAO dao;
    @Resource
    protected MessageMassDAO messageMassDAO;
    @Resource
    protected MessageMassRepo messageMassRepo;
    @Resource
    protected MessageTemplateRepo messageTemplateRepo;

    public MassTactic(Long id) {
        super(id);
    }

    public MassTacticDO data() {
        return this.dao.selectById(this.id());
    }

    /**
     * 测试此策略
     */
    public final MessageMass test() {
        throw new NotImplementedException();
    }

    /**
     * @deprecated use {@link #execAsync()} instead
     */
    public final MessageMass exec() throws ExecException {
        return this.exec(false);
    }

    /**
     * 执行此策略
     *
     * @param boost 是否加速分发（例如切片并行操作）
     * @deprecated use {@link #execAsync()} instead
     */
    public final MessageMass exec(boolean boost) throws ExecException {
        if (this.getSourceSize() > PartitionMessageMass.DEFAULT_SUB_MASS_SIZE) {
            log.info("检测到策略 {} 消息源 size 大于 {}，将强制执行 boost 分发", this.id(), PartitionMessageMass.DEFAULT_SUB_MASS_SIZE);
            boost = true;
        }

        if (this.getSourceSize() > 1000) {
            throw new ExecException("数据量过大，需使用异步执行");
        }

        MassTacticDO data = this.data();
        if (Boolean.TRUE.equals(data.getMustTest())) {
            if (!Boolean.TRUE.equals(data.getHasTest())) {
                throw new ExecException("群发策略 %d 必须先通过测试才能执行", this.id());
            }
        }
        try {
            // prepare
            MessageMass mass = this.prepare(boost);

            // set status
            this.markExecuting();

            // deliver
            mass.deliver();
            return mass;
        } catch (PrepareException | MessageMass.DeliverException e) {
            throw new ExecException(e);
        }
    }

    /**
     * 异步执行此策略
     */
    public final CompletableFuture<MessageMass> execAsync() throws ExecException {
        MassTacticDO data = this.data();
        if (Boolean.TRUE.equals(data.getMustTest())) {
            if (!Boolean.TRUE.equals(data.getHasTest())) {
                throw new ExecException("群发策略 %d 必须先通过测试才能执行", this.id());
            }
        }

        if (this.isExecuting()) {
            throw new ExecException("策略 %d 目前正在执行，请勿重复操作", this.id());
        }

        CompletableFuture<MessageMass> async;
        if (this.hasPrepared()) {
            async = CompletableFuture.supplyAsync(() -> {
                return this.getPreparedMass()
                        .orElseThrow(() -> new DataErrorException("群发策略", this.id(), "hasPrepared 但消息集不存在"));
            });
        } else {
            async = CompletableFuture.supplyAsync(() -> {
                // 后续可能存在并行操作，依赖于创建 mass 的事务提交，因此需要单独执行
                return this.newBoostMass();
            });
        }

        // TODO:: 单独创建的实体都是没有经过 aop 的，这样执行是否存在事务？表示怀疑。另外要确认无事务是否会导致问题
        return async.thenApplyAsync(mass -> {
            // prepare & deliver 异步执行，不阻塞调用方获取 mass
            CompletableFuture.runAsync(() -> {
                if (!mass.hasPrepared()) {
                    if (mass instanceof PartitionMessageMass) {
                        ((PartitionMessageMass) mass).prepare(true);
                    } else {
                        mass.prepare();
                    }
                }
            }).thenRunAsync(() -> {
                this.markExecuting();
                if (mass instanceof PartitionMessageMass) {
                    ((PartitionMessageMass) mass).deliver(true);
                } else {
                    mass.deliver();
                }
            }).exceptionally(e -> {
                log.warn(String.format("群发策略 %d 异步执行出现异常", this.id()), e);
                // 重置状态
                this.updateStatus(Status.AVAILABLE);
                return null;
            });

            return mass;
        });
    }

    /**
     * @deprecated will remove on 0.3
     */
    public final MessageMass prepare() throws PrepareException {
        return this.prepare(false);
    }

    /**
     * 执行策略准备工作（单独执行在海量消息群发场景有助于降低发送时延）
     *
     * @param boost 是否加速
     * @deprecated will remove on 0.3
     */
    public final MessageMass prepare(boolean boost) throws PrepareException {
        if (this.isExecuting()) {
            throw new PrepareException("策略 %d 目前正在执行，请勿重复操作", this.id());
        }

        if (this.hasPrepared()) {
            return this.getPreparedMass()
                    .orElseThrow(() -> new DataErrorException("群发策略", this.id(), "hasPrepared 但消息集不存在"));
        }
        MessageMass preparedMass = this.doPrepare(boost);
        this.markPrepared(preparedMass);
        return preparedMass;
    }

    /**
     * 归档此策略
     */
    public final void archived() {
        if (this.isExecuting()) {
            throw new EndException("策略 %d 正在执行中，请等待执行完毕再归档", this.id());
        }

        this.updateStatus(Status.ARCHIVED);
    }

    boolean isExecuting() {
        return this.data().getStatus() == Status.EXECUTING;
    }

    /**
     * 获取已经准备好的消息集合
     */
    Optional<MessageMass> getPreparedMass() {
        Long massId = this.data().getPreparedMassId();
        if (massId == null || massId == 0) {
            return Optional.empty();
        }
        return messageMassRepo.get(massId);
    }

    /**
     * 执行准备工作
     *
     * @deprecated will remove on 0.3
     */
    protected MessageMass doPrepare() {
        return this.doPrepare(false);
    }

    /**
     * 执行准备工作
     *
     * @param boost 是否加速
     * @deprecated will remove on 0.3
     */
    protected MessageMass doPrepare(boolean boost) {
        MessageMass mass;
        if (boost) {
            mass = this.newBoostMass();
        } else {
            mass = this.newMass();
        }

        mass.prepare();
        return mass;
    }

    /**
     * 标记为已准备好
     *
     * @param mass 准备好的消息集
     */
    void markPrepared(MessageMass mass) {
        MassTacticDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setPreparedMassId(mass.getId());
        o.setStatus(Status.PREPARED);
        this.publish(new PreparedEvent());
        this.dao.updateById(o);
    }

    private void updateStatus(Status status) {
        if (this.data().getStatus() != status) {
            MassTacticDO o = dao.newEmptyDataObject();
            o.setId(this.id());
            o.setStatus(status);
            this.dao.updateById(o);
        }
    }

    protected MessageMass newMass() {
        return newMass(false);
    }

    /**
     * 创建一个 boost 消息集（仅支持正式发送）
     *
     * @deprecated will remove on 0.3
     */
    protected MessageMass newBoostMass() {
        return newMass();
    }

    /**
     * 创建一个新的消息集
     *
     * @param test 是否测试集
     */
    protected MessageMass newMass(boolean test) {
        MessageTemplate template = this.getMessageTemplate();
        MessageMassDO o = this.messageMassDAO.newEmptyDataObject();
        o.setTest(test);

        if (!test) {
            o.setSize(this.getSourceSize());
            String type = this.data().getType();

            if (StringUtils.isBlank(type)) {
                o.setType("PARTITION");
            } else {
                o.setType(type);
            }
        } else {
            // TODO:: 测试集相关实现
            throw new NotImplementedException();
        }
        o.setMessageType(template.getMessageType());
        o.setSpType(template.data().getSpType());
        o.setTacticId(this.id());
        return this.messageMassRepo.create(o);
    }

    int getSourceSize() {
        Integer size = this.data().getSourceSize();
        if (size == null) {
            log.debug("策略 {} 消息源 size 值缓存不存在，重新计算", this.id());
            Date start = new Date();
            size = this.getSource().size();
            log.debug("计算完成，size: {}，总耗时 {}ms", size, System.currentTimeMillis() - start.getTime());
            // update cache
            MassTacticDO o = dao.newEmptyDataObject();
            o.setId(this.id());
            o.setSourceSize(size);
            this.dao.updateById(o);
        }

        return size;
    }

    public List<MessageInfo> listMessageInfos(Integer start, Integer end) {
        MessageInfo def = new MessageInfo();
        def.setSender(this.data().getDefaultSender());
        def.setParams(this.data().getDefaultParams());
        def.setSignature(this.data().getDefaultSignature());
        def.setExt(this.data().getDefaultExt());
        return this.getMessageTemplate()
                .resolve(start, end, this.getSource(), def);
    }

    public List<MessageInfo> listMessageInfos() {
        MessageInfo def = new MessageInfo();
        def.setSender(this.data().getDefaultSender());
        def.setParams(this.data().getDefaultParams());
        return this.getMessageTemplate()
                .resolve(this.getSource(), def);
    }

    public MessageTemplate getMessageTemplate() {
        return this.messageTemplateRepo.getOrThrow(this.data().getTemplateId());
    }

    private Source getSource() {
        MassTacticDO data = this.data();

        switch (data.getSourceType()) {
            case TEXT:
                return new Source.Text(data.getSource());
            case FILE:
                return new Source.File(data.getSource());
            case URL:
                return new Source.Url(data.getSource());
            default:
                throw new UnsupportedOperationException(data.getSourceType().name());
        }
    }

    /**
     * 判断策略是否已完成准备工作
     */
    boolean hasPrepared() {
        return Lists.newArrayList(Status.PREPARED, Status.EXECUTING)
                .contains(this.data().getStatus());
    }

    /**
     * 将此策略标记为正在执行中
     */
    public void markExecuting() {
        this.updateStatus(Status.EXECUTING);
    }

    /**
     * 将此策略标记为可用
     */
    public void setAvailable() {
        this.updateStatus(Status.AVAILABLE);
        MassTacticDO o = dao.newEmptyDataObject();
        o.setStatus(Status.AVAILABLE);
        // TODO:: mp 更新为 null 比较麻烦，先置为 -1
        o.setPreparedMassId(-1L);
        this.dao.updateById(o);
    }

    /**
     * 执行次数 +1
     */
    public void increaseTimes() {
        MassTacticDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setExecTimes(Optional.ofNullable(this.data().getExecTimes()).orElse(0) + 1);
        this.dao.updateById(o);
    }

    /**
     * 群发策略状态
     */
    public enum Status {
        /**
         * 可用
         */
        AVAILABLE,
        /**
         * 已完成执行前准备工作
         */
        PREPARED,
        /**
         * 执行中
         */
        EXECUTING,
        /**
         * 已归档
         */
        ARCHIVED;
    }


    /**
     * 数据源类型
     */
    public enum SourceType {
        TEXT,
        FILE,
        URL
    }

    public static class PreparedEvent extends Event.Base<MassTactic> {
    }

    /**
     * 策略执行异常
     */
    @ErrorCode(value = "MASS_TACTIC_EXEC", description = "群发策略执行失败")
    public static class ExecException extends DomainException {
        public ExecException(String message, Object... args) {
            super(message, args);
        }

        public ExecException(Throwable cause) {
            super("策略执行失败", cause);
        }

        public ExecException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 策略准备工作异常
     */
    @ErrorCode(value = "MASS_TACTIC_PREPARE", description = "群发策略准备失败")
    public static class PrepareException extends DomainException {
        public PrepareException(String message, Object... args) {
            super(message, args);
        }
    }

    /**
     * 结束策略异常
     */
    @ErrorCode(value = "MASS_TACTIC_END", description = "群发策略结束失败")
    public static class EndException extends DomainException {
        public EndException(String message, Object... args) {
            super(message, args);
        }
    }

    public static class Default extends MassTactic {
        @Resource
        private TransactionWrapper transactionWrapper;

        public Default(Long id) {
            super(id);
        }
    }
}
