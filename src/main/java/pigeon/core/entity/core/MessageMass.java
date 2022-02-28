package pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.domain.core.EventPublisher;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import pigeon.core.data.MessageMassDO;

import java.util.List;
import java.util.Optional;

/**
 * 消息集（代表海量消息组成的消息集合）
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageMass extends Entity<Long>, EventPublisher {
    MessageMassDO data();

    /**
     * 获取此集合下的所有消息实体
     *
     * @return 消息实体列表
     */
    default List<Message> listMessages() {
        return this.listMessages(100L);
    }

    /**
     * 获取此集合下的所有消息实体（注意：消息的数量巨大时性可能会十分低下）
     *
     * @param limit 最大数量，建议不要超过 100
     * @return 消息实体列表
     */
    List<Message> listMessages(long limit);

    /**
     * 投递此消息集
     */
    void deliver() throws DeliverException;

    /**
     * @return 集合大小
     */
    int size();

    /**
     * 根据实际的消息数重新刷新统计结果，如状态、总数等（一般作为执行发生错误导致数据不准确时的补偿操作）
     */
    void refreshStat();

    /**
     * 批量将消息添加到此集合
     *
     * @param messages 消息
     */
    void addAll(List<Message> messages);

    /**
     * 标识消息集合为已经准备完毕
     */
    void markPrepared();

    /**
     * 标识消息集合状态为已分发
     */
    void markDelivered();

    /**
     * 标识消息集合状态为已分发
     */
    default void markDeliveredAndPublicEvent() {
        this.markDelivered();
        this.publish(new DeliveredEvent());
    }

    /**
     * 获取此消息集归属的群发策略实体
     *
     * @return 此消息集关联的策略
     */
    Optional<MassTactic> getTactic();

    void prepare();

    /**
     * 判断 mass 是否已 prepared
     *
     * @return true or false
     */
    boolean hasPrepared();

    /**
     * 消息集全部分发完成事件
     */
    class DeliveredEvent extends Event.Base<MessageMass> {
    }

    /**
     * 消息集部分分发完成事件
     */
    class PartDeliveredEvent extends Event.Base<MessageMass> {
    }

    /**
     * 消息集状态
     */
    enum Status {
        /**
         * 创建中
         */
        CREATING,
        /**
         * 未投递
         */
        NOT_DELIVERED,
        /**
         * 投递中
         */
        DELIVERING,
        /**
         * 已投递
         */
        ALL_DELIVERED;
    }

    class StartDeliverEvent extends Event.Base<MessageMass> {
    }

    class PreparedEvent extends Event.Base<MessageMass> {
    }

    @ErrorCode(value = "MESSAGE_MASS_DELIVERY", description = "消息集投递")
    class DeliverException extends DomainException {
        public DeliverException(String message) {
            super(message);
        }

        public DeliverException(String message, Object... args) {
            super(message, args);
        }

    }
}
