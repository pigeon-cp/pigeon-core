package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.google.common.eventbus.Subscribe;

public interface DomainEventSubscriber {
    @Subscribe
    default void listen(Message.DeliverEvent e) throws Throwable {
    }

    @Subscribe
    default void listen(Message.SentEvent e) throws Throwable {
    }
}
