package com.github.taccisum.pigeon.core.event.handler;

import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import com.github.taccisum.pigeon.core.entity.core.mass.PartitionMessageMass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 该 subscriber 负责维护群发集合状态
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Slf4j
@Component
public class MessageMassStatusMaintainer implements DomainEventSubscriber {
    @Override
    public void listen(SubMass.DeliveredEvent e) throws Throwable {
        SubMass sub = e.getPublisher();
        MessageMass mass = sub.getMain();

        if (mass instanceof PartitionMessageMass) {
            log.debug("消息子集合 {} 分发完毕，将根据结果对主集合 {} 状态进行变更", sub.id(), mass.id());
            ((PartitionMessageMass) mass).markPartDelivered(sub, e.getFailCount());
        } else {
            log.warn("消息子集合 {} 所属主集合 {} 非分片集合，请检查数据是否异常", sub.id(), mass.id());
        }
    }
}
