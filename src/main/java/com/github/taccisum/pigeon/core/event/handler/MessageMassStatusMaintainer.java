package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import org.springframework.stereotype.Component;

/**
 * 该 subscriber 负责维护群发集合状态
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Component
public class MessageMassStatusMaintainer implements DomainEventSubscriber {
    @Override
    public void listen(MessageMass.DeliveredEvent e) throws Throwable {
        e.getPublisher().markDelivered();
    }
}
