package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;
import org.pf4j.Extension;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageTemplateFactory extends EntityFactory<Long, MessageTemplate, MessageTemplateFactory.Criteria> {
    @Data
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
        public MessageTemplate create(Long id, Criteria criteria) {
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
