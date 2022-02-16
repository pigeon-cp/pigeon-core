package pigeon.core.repo.factory;

import pigeon.core.entity.core.MassTactic;
import pigeon.core.repo.EntityFactory;
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
            return new MassTactic.Default(id);
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
