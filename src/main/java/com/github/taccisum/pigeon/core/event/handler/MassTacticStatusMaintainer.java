package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import org.springframework.stereotype.Component;

/**
 * 该 subscriber 负责维护群发策略状态
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MassTacticStatusMaintainer implements DomainEventSubscriber {
    @Override
    public void listen(MessageMass.StartDeliverEvent e) throws Throwable {
        MassTactic tactic = e.getPublisher().getTactic()
                .orElse(null);
        if (tactic != null) {
            tactic.markExecuting();
        }
    }

    @Override
    public void listen(MessageMass.DeliveredEvent e) throws Throwable {
        MassTactic tactic = e.getPublisher().getTactic()
                .orElse(null);
        if (tactic != null) {
            tactic.setAvailable();
            tactic.increaseTimes();
        }
    }
}
