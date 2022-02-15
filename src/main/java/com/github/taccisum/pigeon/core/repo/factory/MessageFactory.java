package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.message.ConsoleMessage;
import com.github.taccisum.pigeon.core.entity.core.message.DummyMessage;
import com.github.taccisum.pigeon.core.entity.core.message.LogMessage;
import com.github.taccisum.pigeon.core.entity.core.message.Mail;
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
            String type = Optional.ofNullable(criteria.getType()).orElse("");
            switch (type.toUpperCase()) {
                case "CONSOLE":
                    return new ConsoleMessage(id);
                case "LOG":
                    return new LogMessage(id);
                case "DUMMY":
                    return new DummyMessage(id);
                default:
                    throw new UnsupportedOperationException(type);
            }
        }

        @Override
        public boolean match(Long id, Criteria criteria) {
            return "PIGEON".equalsIgnoreCase(Optional.ofNullable(criteria.getSpType()).orElse(""));
        }
    }

    @Extension
    class Default implements MessageFactory {
        @Override
        public Message create(Long id, Criteria criteria) {
            String type = Optional.ofNullable(criteria.getType()).orElse("");
            switch (type.toUpperCase()) {
                case Message.Type.MAIL:
                    return new Mail.Default(id);
                default:
                    throw new UnsupportedOperationException(type);
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
