package com.github.taccisum.pigeon.core.valueobj;

import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.RawMessageDeliverer;
import com.github.taccisum.pigeon.core.entity.core.holder.MessageDelivererHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * 由同类消息组成的消息集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Slf4j
public class Messages {
    /**
     * 消息元素
     */
    private List<Message> messages = new ArrayList<>();
    /**
     * 包含的消息类型，手动指定或添加第一条消息后自动识别
     */
    @Getter
    private Class<? extends Message> type;
    /**
     * 最后一次分发的失败次数
     */
    @Getter
    private int lastDeliverFailCount;

    public Messages(List<Message> messages) {
        this(messages, null);
    }

    public Messages(List<Message> messages, Class<? extends Message> type) {
        this.type = type;
        this.addAll(messages);
    }

    public int size() {
        return messages.size();
    }

    /**
     * 获取消息原始数据集
     */
    public List<MessageDO> listRaw() {
        return this.messages.stream()
                .map(Message::data)
                .collect(Collectors.toList());
    }

    /**
     * 将所有消息添加到此消息集中
     */
    public void addAll(List<Message> messages) {
        this.checkSameType(messages);
        this.messages.addAll(messages);
    }

    private void checkSameType(List<Message> messages) {
        if (messages.size() == 0) {
            return;
        }
        if (type == null) {
            type = messages.get(0).getClass();
        }

        Set<? extends Class<? extends Message>> types = messages.stream()
                .map(Message::getClass)
                .collect(Collectors.toSet());

        if (types.size() > 1) {
            throw new IllegalArgumentException("不允许包含不同类型的消息");
        }

        if (!Objects.equals(type, types.iterator().next())) {
            // 类型必须完全相同，即使是子类也不允许
            throw new IllegalArgumentException(String.format("不允许添加非 %s 类型的消息", this.type));
        }
    }

    /**
     * 获取合适的 Raw 消息分发器，不存在时返回 null
     */
    public RawMessageDeliverer findSuitableRawMessageDeliverer() {
        if (this.isEmpty()) {
            return null;
        }
        if (MessageDelivererHolder.class.isAssignableFrom(type)) {
            return ((MessageDelivererHolder) this.messages.get(0)).getMessageDeliverer();
        }

        return null;
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return messages.size() == 0;
    }

    /**
     * 批量分发消息
     */
    public void deliver() {
        if (this.isEmpty()) {
            return;
        }

        RawMessageDeliverer deliverer = this.findSuitableRawMessageDeliverer();

        if (deliverer != null) {
            // TODO::
            deliverer.deliverBatchFast(this.listRaw());
//                    failCount =
        } else {
            log.warn("消息类型 {} 无合适的 Raw 消息分发器，无法执行批量发送，将回退到原始的逐条发送方式（注意：此方式性能极为低下！）", this.getType().getName());
            ForkJoinPool poll = new ForkJoinPool();
            try {
                lastDeliverFailCount = poll.submit(
                        () -> messages.parallelStream()
                                .map(message -> {
                                    try {
                                        message.deliver();
                                        return true;
                                    } catch (Exception e) {
                                        log.error(String.format("消息 %d 发送失败", message.id()), e);
                                        return false;
                                    }
                                })
                                .map(success -> !success ? 1 : 0)
                                .reduce(Integer::sum)
                                .orElse(0)
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO::
                e.printStackTrace();
            }
        }
    }
}
