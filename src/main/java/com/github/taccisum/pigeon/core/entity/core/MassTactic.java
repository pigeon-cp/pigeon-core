package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.dao.MassTacticDAO;
import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MassTacticDO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.repo.MessageMassRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.MessageTemplateRepo;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import com.github.taccisum.pigeon.core.valueobj.Source;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 消息群发策略
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MassTactic extends Entity.Base<Long> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private MassTacticDAO dao;
    @Resource
    private MessageMassDAO messageMassDAO;
    @Resource
    private MessageMassRepo messageMassRepo;
    @Resource
    private MessageTemplateRepo messageTemplateRepo;

    public MassTactic(Long id) {
        super(id);
    }

    public MassTacticDO data() {
        return this.dao.selectById(this.id());
    }

    /**
     * 测试此策略
     */
    public MessageMass test() {
        throw new NotImplementedException();
    }

    /**
     * 执行此策略
     */
    public MessageMass exec() throws ExecException {
        MassTacticDO data = this.data();
        if (Boolean.TRUE.equals(data.getMustTest())) {
            if (!Boolean.TRUE.equals(data.getHasTest())) {
                throw new ExecException("群发策略 %d 必须先通过测试才能执行", this.id());
            }
        }
        MessageMass mass = null;
        try {
            mass = this.prepare();
            mass.deliver();
            return mass;
        } catch (PrepareException e) {
            throw new ExecException(e);
        }
    }

    /**
     * 执行策略准备工作（在海量消息群发场景有助于降低发送时延）
     */
    public MessageMass prepare() throws PrepareException {
        if (this.isExecuting()) {
            throw new PrepareException("策略 %d 目前正在执行，请勿重复操作", this.id());
        }

        if (this.hasPrepared()) {
            return this.getPreparedMass()
                    .orElseThrow(() -> new DataErrorException("群发策略", this.id(), "hasPrepared 但消息集不存在"));
        }
        return this.doPrepare();
    }

    /**
     * 归档此策略
     */
    public void archived() {
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
        MassTacticDO data = this.data();
        MessageMass mass = this.newMass();
        MessageTemplate template = this.getMessageTemplate();
        List<Message> messages = new ArrayList<>();
        // TODO:: 针对海量目标群进行性能优化
        for (MessageInfo info : this.listMessageInfos()) {
            // TODO:: magic num
            try {
                if (info.getAccount() instanceof User) {
                    messages.add(template.initMessage(info.getSender(), (User) info.getAccount(), info.getParams()));
                } else {
                    messages.add(template.initMessage(info.getSender(), (String) info.getAccount(), info.getParams()));
                }
            } catch (MessageRepo.CreateMessageException e) {
                log.warn("发送至 {} 的消息创建失败：{}", info, e.getMessage());
            }
        }

        mass.addAll(messages);
        this.markPrepared(mass);
        return mass;
    }

    void markPrepared(MessageMass mass) {
        MassTacticDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setPreparedMassId(mass.getId());
        o.setStatus(Status.PREPARED);
        mass.markPrepared();
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

    MessageMass newMass() {
        return newMass(false);
    }

    private MessageMass newMass(boolean test) {
        MessageMassDO o = messageMassDAO.newEmptyDataObject();
        o.setTest(test);
        o.setSize(0);
        o.setTacticId(this.id());
        return messageMassRepo.create(o);
    }

    List<MessageInfo> listMessageInfos() {
        MessageInfo def = new MessageInfo();
        def.setSender(this.data().getDefaultSender());
        def.setParams(this.data().getDefaultParams());
        return this.getMessageTemplate()
                .resolve(this.getSource(), def);
    }

    MessageTemplate getMessageTemplate() {
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
        // TODO:: mp 更新为 null 比较麻烦，先置为 0
        o.setPreparedMassId(0L);
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
        public Default(Long id) {
            super(id);
        }
    }
}
