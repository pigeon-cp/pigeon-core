package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.message.ConsoleMessage;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;
import org.pf4j.Extension;

import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageFactory extends EntityFactory<Long, Message, MessageFactory.Criteria> {
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
    class Console implements MessageFactory {
        @Override
        public Message create(Long id, Criteria criteria) {
            return new ConsoleMessage(id);
        }

        @Override
        public boolean match(Long id, Criteria criteria) {
            return "CONSOLE".equalsIgnoreCase(Optional.ofNullable(criteria.getType()).orElse(""))
                    && "PIGEON".equalsIgnoreCase(Optional.ofNullable(criteria.getSpType()).orElse(""))
                    ;
        }
    }
}
