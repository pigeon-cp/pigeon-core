package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.holder.MessageDelivererHolder;
import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import com.github.taccisum.pigeon.core.repo.MessageTemplateRepo;
import com.github.taccisum.pigeon.core.repo.ServiceProviderRepo;
import com.github.taccisum.pigeon.core.repo.ThirdAccountRepo;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * 业务消息基类，可以是短信、推送、微信等等（取决于具体实现）
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class Message extends Entity.Base<Long> {
    @Setter
    private MessageDO data;
    public static final String DEFAULT_SENDER = "pigeon";
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
        if (data == null) {
            return this.dao.selectById(this.id());
        }
        return data;
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
        MessageDO data = this.data();
        if (this.shouldRelateTemplate()) {
            if (data.getTemplateId() == null) {
                throw new DeliverException("消息 %d 必须关联模板", this.id());
            }
        }

        Timer timer = Timer.builder("message.delivery")
                .description("消息投递（delivery）耗费时间")
                .tag("class", this.getClass().getName())
                .tag("type", data.getType())
                .tag("sp", data.getSpType())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        return timer.record(() -> {
            boolean success = false;
            String msg = null;
            try {
                this.doDelivery();
                success = true;
            } catch (DomainException e) {
                log.warn(String.format("消息 %d 发送失败", this.id()), e);
                msg = e.getMessage();
            } catch (Exception e) {
                log.error(String.format("消息 %d 发送时发生错误", this.id()), e);
                msg = e.getMessage();
            }

            if (this.isRealTime()) {
                log.debug("消息 {} 为实时消息，将直接标记发送结果", this.id());
                if (msg != null) {
                    this.markSent(success, msg);
                }
                this.markSent(success);
            } else {
                log.debug("消息 {} 为非实时消息，仅标记投递结果", this.id());
                this.markDelivered(success, msg);
            }

            return success;
        });
    }

    /**
     * <pre>
     * 判断此消息是否必须要关联模板
     *
     * 有些消息（如短信）可能必需要提前在运营商备案模板方可发送，此时应返回 true，其它返回 false
     * </pre>
     */
    public boolean shouldRelateTemplate() {
        return false;
    }

    /**
     * <pre>
     * 判断此消息是否实时消息，会影响到部分业务逻辑，如
     *
     * * 在消息分发成功后，实时消息直接变为完成状态，而非实时消息则是变为已分发状态（一般是接受到第三方待异步回调通知后再变更为完成）
     * </pre>
     */
    public abstract boolean isRealTime();

    /**
     * 执行消息投递的具体逻辑
     */
    protected void doDelivery() throws Exception {
        if (this instanceof MessageDelivererHolder) {
            ((MessageDelivererHolder) this).getMessageDeliverer().deliver(this.data());
        }
        throw new NotImplementedException("You should impl #doDelivery by yourself if relative message sender could not be found.");
    }

    /**
     * 获取此消息关联的服务提供商
     */
    public abstract MessageServiceProvider getServiceProvider();

    /**
     * 获取发送此消息所使用的服务商账号
     */
    public ThirdAccount getSpAccount() throws ThirdAccountRepo.NotFoundException {
        return this.getServiceProvider()
                .getAccountOrThrow(this.data().getSpAccountId());
    }

    /**
     * 获取此消息关联的模板
     */
    public MessageTemplate getMessageTemplate() throws MessageTemplateRepo.MessageTemplateNotFoundException {
        if (this.shouldRelateTemplate()) {
            return messageTemplateRepo.getOrThrow(this.data().getTemplateId());
        }
        return messageTemplateRepo.get(this.data().getTemplateId())
                .orElse(null);
    }

    /**
     * 更新消息状态
     *
     * @param status 目标状态
     */
    protected void updateStatus(Status status) {
        this.updateStatus(status, "-");
    }

    /**
     * 更新消息状态及状态信息
     *
     * @param status 目标状态
     * @param msg    状态信息
     */
    protected void updateStatus(Status status, String msg) {
        MessageDO o = new MessageDO();
        o.setId(this.id());
        o.setStatus(status);
        o.setStatusRemark(msg);
        this.dao.updateById(o);
    }

    /**
     * 标记消息为已发送
     *
     * @param success 是否成功
     */
    public void markSent(boolean success) {
        this.markSent(success, success ? "发送成功" : "发送失败，原因未知");
    }

    /**
     * 标记消息为已发送
     *
     * @param success 是否成功
     * @param msg     状态信息
     */
    public void markSent(boolean success, String msg) {
        this.updateStatus(success ? Status.SENT : Status.FAIL, msg);
        this.publish(new SentEvent(success));
    }


    /**
     * 标记消息为已投递
     *
     * @param success 是否成功
     * @param msg     状态信息
     */
    public void markDelivered(boolean success, String msg) {
        this.updateStatus(success ? Status.DELIVERED : Status.FAIL, msg);
        this.publish(new DeliverEvent(success));
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

    /**
     * 消息分发异常
     */
    @ErrorCode(value = "MESSAGE_DELIVERY", description = "消息投递")
    public static class DeliverException extends DomainException {
        public DeliverException(String message) {
            super(message);
        }

        public DeliverException(String message, Object... args) {
            super(message, args);
        }
    }
}
