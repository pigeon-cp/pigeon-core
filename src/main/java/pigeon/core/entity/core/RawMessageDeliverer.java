package pigeon.core.entity.core;

import pigeon.core.data.MessageDO;

import java.util.List;

/**
 * <pre>
 * {@link MessageMass} 专用 Raw 消息分发器
 *
 * 区别于消息本身的分发能力 {@link Message#deliver()}，此分发器将直接对批
 * 量的原始数据（{@link MessageDO}）进行操作，由此避免逐条处理时
 * 可能会带来的不必要的网络传输损耗（如针对 id 的数据库单点查询） 等，由此来提高性能
 * </pre>
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @see MessageMass
 * @since 0.2
 * @deprecated 应通过实现 sub mass 的子类来解决 raw 群发的问题
 */
public interface RawMessageDeliverer {
    /**
     * 投递消息
     *
     * @param message 消息数据对象
     * @return delivery id
     */
    String deliver(MessageDO message);

    /**
     * <pre>
     * 批量投递消息（高性能版）
     *
     * 为了进一步提高性能，建议实现此方法时尽可能减少对消息的 raw data 进行 check 操作，框架会在
     * 消息集的 {@link MessageMass#prepare()} 阶段调用相关的逻辑以尽可能保证数据是符合预期的
     * </pre>
     *
     * @param messages 消息数据对象列表
     * @return delivery id
     */
    String deliverBatchFast(List<MessageDO> messages);
}
