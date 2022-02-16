package pigeon.core.event.handler;

import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.MessageMass;
import pigeon.core.entity.core.SubMass;
import com.google.common.eventbus.Subscribe;

public interface DomainEventSubscriber {
    @Subscribe
    default void listen(Message.DeliverEvent e) throws Throwable {
    }

    @Subscribe
    default void listen(Message.SentEvent e) throws Throwable {
    }

    @Subscribe
    default void listen(MessageMass.StartDeliverEvent e) throws Throwable {
    }

    @Subscribe
    default void listen(MessageMass.DeliveredEvent e) throws Throwable {
    }

    @Subscribe
    default void listen(SubMass.DeliveredEvent e) throws Throwable {
    }
}
