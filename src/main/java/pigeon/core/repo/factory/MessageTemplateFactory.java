package pigeon.core.repo.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.pf4j.Extension;
import pigeon.core.entity.core.MessageTemplate;
import pigeon.core.repo.EntityFactory;
import pigeon.core.repo.factory.MessageTemplateFactory.Criteria;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageTemplateFactory extends EntityFactory<Long, MessageTemplate, Criteria> {
    @Data
    @Accessors(chain = true)
    class Criteria {
        private String type;
        private String spType;

        public Criteria(String type, String spType) {
            this.type = type;
            this.spType = spType;
        }
    }

    @Extension
    class Default implements MessageTemplateFactory {
        @Override
        public MessageTemplate create(Long id, MessageTemplateFactory.Criteria criteria) {
            return new MessageTemplate.Default(id);
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
