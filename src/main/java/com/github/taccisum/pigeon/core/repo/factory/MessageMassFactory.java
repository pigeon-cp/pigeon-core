package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import com.github.taccisum.pigeon.core.entity.core.mass.AbstractMessageMass;
import com.github.taccisum.pigeon.core.entity.core.mass.PartitionMessageMass;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;
import lombok.experimental.Accessors;
import org.pf4j.Extension;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface MessageMassFactory extends EntityFactory<Long, MessageMass, MessageMassFactory.Criteria> {
    @Data
    @Accessors(chain = true)
    class Criteria {
        private String type;
        private String spType;
        private String messageType;
    }

    @Extension
    class Default implements MessageMassFactory {
        private static final Default INS = new Default();

        public static Default instance() {
            return INS;
        }

        @Override
        public MessageMass create(Long id, Criteria criteria) {
            switch (criteria.getType()) {
                case "PARTITION":
                    return new PartitionMessageMass(id);
                case "DEFAULT":
                default:
                    return new AbstractMessageMass.Default(id);
            }
        }

        @Override
        public boolean match(Long id, Criteria criteria) {
            return true;
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }
    }
}
