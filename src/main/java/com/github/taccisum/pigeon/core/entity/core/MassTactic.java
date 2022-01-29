package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.dao.MassTacticDAO;
import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MassTacticDO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.repo.MessageMassRepo;
import com.github.taccisum.pigeon.core.repo.MessageTemplateRepo;
import com.github.taccisum.pigeon.core.service.TransactionWrapper;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import com.github.taccisum.pigeon.core.valueobj.Source;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.taccisum.pigeon.core.entity.core.mass.PartitionMessageMass.SUB_MASS_SIZE;

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

    public final MessageMass exec() throws ExecException {
        return this.exec(false);
    }

    /**
     * 执行此策略
     */
    public final MessageMass exec(boolean boost) throws ExecException {
        if (this.getSourceSize() > SUB_MASS_SIZE) {
            log.info("检测到策略 {} 消息源 size 大于 {}，将强制执行 boost 分发", this.id(), SUB_MASS_SIZE);
            boost = true;
        }

        MassTacticDO data = this.data();
        if (Boolean.TRUE.equals(data.getMustTest())) {
            if (!Boolean.TRUE.equals(data.getHasTest())) {
                throw new ExecException("群发策略 %d 必须先通过测试才能执行", this.id());
            }
        }
        try {
            StopWatch sw = new StopWatch();

            // prepare
            sw.start();
            MessageMass mass = this.prepare(boost);
            sw.stop();
            log.debug("策略 {} 执行 prepare 阶段完成，总耗时 {}ms", this.id(), sw.getLastTaskTimeMillis());

            // set status
            this.markExecuting();

            // deliver
            sw.start();
            mass.deliver();
            sw.stop();
            log.debug("消息集 {} 分发完成，总耗时 {}ms", mass.id(), sw.getLastTaskTimeMillis());
            return mass;
        } catch (PrepareException | MessageMass.DeliverException e) {
            throw new ExecException(e);
        }
    }

    public final MessageMass prepare() throws PrepareException {
        return this.prepare(false);
    }

    /**
     * 执行策略准备工作（在海量消息群发场景有助于降低发送时延）
     *
     * @param boost 是否加速
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
     */
    protected MessageMass doPrepare() {
        return this.doPrepare(false);
    }

    /**
     * 执行准备工作
     *
     * @param boost 是否加速
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

    protected MessageMass newBoostMass() {
        MessageMassDO o = this.messageMassDAO.newEmptyDataObject();
        o.setTest(false);
        o.setSize(this.getSourceSize());
        o.setType("PARTITION");
        o.setTacticId(this.id());
        return this.messageMassRepo.create(o);
    }

    /**
     * 创建一个新的消息集
     *
     * @param test 是否测试集
     */
    protected MessageMass newMass(boolean test) {
        MessageMassDO o = this.messageMassDAO.newEmptyDataObject();
        o.setType("DEFAULT");
        o.setTest(test);
        if (!test) {
            o.setSize(this.getSourceSize());
        }
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

    /**
     * 策略执行异常
     */
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
    public static class PrepareException extends DomainException {
        public PrepareException(String message, Object... args) {
            super(message, args);
        }
    }

    /**
     * 结束策略异常
     */
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
