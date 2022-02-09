package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.stereotype.Component;

/**
 * 消息统计订阅者
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Component
public class MessageStatSubscriber implements DomainEventSubscriber {
    @Override
    public void listen(Message.DeliverEvent e) throws Throwable {
        Message message = e.getPublisher();
        MessageDO data = message.data();
        Counter counter = Counter.builder("message.delivery")
                .description("消息投递数量统计")
                .tag("type", data.getType())
                .tag("sp", data.getSpType())
                .tag("ismass", data.getMassId() != null ? "TRUE" : "FALSE")
                .tag("success", e.getSuccess() ? "TRUE" : "FALSE")
                .register(Metrics.globalRegistry);
        counter.increment();
    }

    @Override
    public void listen(Message.SentEvent e) throws Throwable {
        Message message = e.getPublisher();
        MessageDO data = message.data();
        Counter counter = Counter.builder("message.sending")
                .description("消息发送数量统计")
                .tag("type", data.getType())
                .tag("sp", data.getSpType())
                .tag("ismass", data.getMassId() != null ? "TRUE" : "FALSE")
                .tag("success", e.getSuccess() ? "TRUE" : "FALSE")
                .register(Metrics.globalRegistry);
        counter.increment();
    }
}
