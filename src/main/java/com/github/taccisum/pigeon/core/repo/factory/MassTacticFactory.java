package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import com.github.taccisum.pigeon.core.entity.core.mass.MassNow;
import com.github.taccisum.pigeon.core.entity.core.mass.MassOnDelay;
import com.github.taccisum.pigeon.core.entity.core.mass.MassOnTime;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import com.google.common.collect.Lists;
import lombok.Data;
import org.pf4j.Extension;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MassTacticFactory extends EntityFactory<Long, MassTactic, MassTacticFactory.Criteria> {
    @Data
    class Criteria {
        private String type;

        public Criteria(String type) {
            this.type = type;
        }
    }

    @Extension
    class Default implements MassTacticFactory {
        private static final Default INS = new Default();

        public static Default instance() {
            return INS;
        }

        @Override
        public MassTactic create(Long id, Criteria criteria) {
            switch (criteria.getType()) {
                case "NOW":
                    return new MassNow(id);
                case "ON_TIME":
                    return new MassOnTime(id);
                case "ON_DELAY":
                    return new MassOnDelay(id);
                default:
                    throw new UnsupportedOperationException(criteria.getType());
            }
        }

        @Override
        public boolean match(Long id, Criteria criteria) {
            return Lists.newArrayList("NOW", "ON_TIME", "ON_DELAY")
                    .contains(criteria.getType());
        }
    }
}
