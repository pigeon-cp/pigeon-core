package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Event;
import com.github.taccisum.domain.core.EventPublisher;
import com.github.taccisum.pigeon.core.data.MessageDO;

import java.util.List;

/**
 * <pre>
 * Raw 消息分发器
 *
 * 区别于消息本身的分发能力 {@link Message#deliver()}，此分发器将直接对批
 * 量的原始数据（{@link MessageDO}）进行操作，由此避免逐条处理时
 * 可能会带来的不必要的网络传输损耗（如针对 id 的数据库单点查询） 等，由此来提高性能
 * </pre>
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface RawMessageDeliverer extends EventPublisher {
    /**
     * 投递消息
     */
    void deliver(MessageDO message);

    /**
     * 批量投递消息，通过简单的 for 循环实现
     * <p>
     * 如果有大量的消息要投递，建议使用 {@link #deliverBatchFast(List)}
     */
    default void deliverBatch(List<MessageDO> messages) {
        for (MessageDO message : messages) {
            this.deliver(message);
        }
    }

    /**
     * 批量投递消息（高性能版）
     */
    void deliverBatchFast(List<MessageDO> messages);

    abstract class Base extends EventPublisher.Base implements RawMessageDeliverer {
    }

    /**
     * 消息分发完成事件
     */
    class DeliveryEvent extends Event.Base<RawMessageDeliverer> {
        private List<MessageDO> messages;
        // TODO::
        private int success = 0;
        private int fail = 0;
        private int error = 0;

        public DeliveryEvent(List<MessageDO> messages, int success, int fail, int error) {
            this.messages = messages;
            this.success = success;
            this.fail = fail;
            this.error = error;
        }
    }
}
