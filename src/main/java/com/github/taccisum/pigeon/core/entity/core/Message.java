package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import com.github.taccisum.pigeon.core.repo.MessageTemplateRepo;
import com.github.taccisum.pigeon.core.repo.ServiceProviderRepo;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 代表一条具体的消息，可以是短信、推送、微信等等
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class Message extends Entity.Base<Long> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    protected MessageDAO dao;
    @Resource
    protected ServiceProviderRepo serviceProviderRepo;
    @Resource
    protected MessageTemplateRepo messageTemplateRepo;

    public Message(Long id) {
        super(id);
    }

    public MessageDO data() {
        return this.dao.selectById(this.id());
    }

    /**
     * <pre>
     * 将此条消息投递到第三方服务商
     *
     * published events:
     * {@link DeliverEvent}
     * </pre>
     *
     * @return 投递结果
     */
    public boolean deliver() {
        boolean success = false;
        try {
            this.doDelivery();
            success = true;
        } catch (DomainException e) {
            log.warn(String.format("消息 %d 发送失败", this.id()), e);
        } catch (Exception e) {
            log.error(String.format("消息 %d 发送时发生错误", this.id()), e);
        }

        if (this.isRealTime()) {
            log.debug("消息 {} 为实时消息，将直接标记发送结果", this.id());
            this.markSent(success);
        } else {
            log.debug("消息 {} 为非实时消息，仅标记投递结果", this.id());
            this.updateStatus(success ? Status.DELIVERED : Status.FAIL);
            this.publish(new DeliverEvent(success));
        }

        return success;
    }

    /**
     * <pre>
     * 判断此消息是否实时消息，会影响到部分业务逻辑，如
     *
     * * 在消息分发成功后，实时消息直接变为完成状态，而非实时消息则是变为已分发状态（一般是接受到第三方待异步回调通知后再变更为完成）
     * </pre>
     */
    protected abstract boolean isRealTime();

    /**
     * 执行消息投递的具体逻辑
     */
    protected abstract void doDelivery() throws Exception;

    /**
     * 获取此消息关联的服务提供商
     */
    protected abstract MessageServiceProvider getServiceProvider();

    /**
     * 获取此消息关联的模板
     */
    protected MessageTemplate getMessageTemplate() {
        return messageTemplateRepo.getOrThrow(this.data().getTemplateId());
    }

    /**
     * 更新消息状态
     *
     * @param status 目标状态
     */
    protected void updateStatus(Status status) {
        MessageDO o = new MessageDO();
        o.setId(this.id());
        o.setStatus(status);
        this.dao.updateById(o);
    }

    /**
     * 标记消息为已发送
     *
     * @param success 是否成功
     */
    public void markSent(boolean success) {
        this.updateStatus(success ? Status.SENT : Status.FAIL);
        this.publish(new SentEvent(success));
    }

    /**
     * TODO:: domain-core 存在 bug，当类多重继承时无法解析到父类的泛型 class type，导致无法自动为事件注入主体，修复好之前无法用这个基类简化代码
     */
    protected static abstract class BaseEvent extends Event.Base<Message> {
    }

    /**
     * 消息投递事件（表示已投递到三方服务商，但不一定已发送）
     */
    public static class DeliverEvent extends Event.Base<Message> {
        /**
         * 表示是否投递成功
         */
        @Getter
        private Boolean success;

        public DeliverEvent(Boolean success) {
            this.success = success;
        }
    }

    /**
     * 消息已发送事件（三方服务商已将消息推送给用户，可能成功也可能失败）
     */
    public static class SentEvent extends Event.Base<Message> {
        /**
         * 表示是否发送成功
         */
        @Getter
        private Boolean success;

        public SentEvent(Boolean success) {
            this.success = success;
        }
    }

    /**
     * 消息类型
     */
    public interface Type {
        /**
         * 邮件
         */
        String MAIL = "MAIL";
        /**
         * 短信
         */
        String SMS = "SMS";
    }

    /**
     * 消息状态
     */
    public enum Status {
        /**
         * 未发送
         */
        NOT_SEND,
        /**
         * 已投递
         */
        DELIVERED,
        /**
         * 已发送
         */
        SENT,
        /**
         * 发送失败
         */
        FAIL,
    }
}
