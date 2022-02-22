package pigeon.core.repo.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.pf4j.Extension;
import pigeon.core.entity.core.SubMass;
import pigeon.core.entity.core.mass.AbstractSubMass;
import pigeon.core.repo.EntityFactory;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface SubMassFactory extends EntityFactory<Long, SubMass, SubMassFactory.Criteria> {
    @Data
    @Accessors(chain = true)
    class Criteria {
        private String spType;
        private String messageType;
    }

    @Extension
    class Default implements SubMassFactory {
        private static final Default INS = new Default();

        public static Default instance() {
            return INS;
        }

        @Override
        public SubMass create(Long id, Criteria criteria) {
            return new AbstractSubMass.Default(id);
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
