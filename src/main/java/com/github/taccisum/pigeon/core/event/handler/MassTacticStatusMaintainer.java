package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 该 subscriber 负责维护群发策略状态
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Slf4j
@Component
public class MassTacticStatusMaintainer implements DomainEventSubscriber {
    @Override
    public void listen(MessageMass.DeliveredEvent e) throws Throwable {
        MessageMass mass = e.getPublisher();
        MassTactic tactic = mass.getTactic()
                .orElse(null);
        if (tactic != null) {
            log.info("消息集 {} 分发完毕，将其所属策略 {} 状态重新置为可用", mass.id(), tactic.id());
            tactic.setAvailable();
            tactic.increaseTimes();
        }
    }
}
