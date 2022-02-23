package pigeon.core.repo.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.pf4j.Extension;
import pigeon.core.docs.DocsSource;
import pigeon.core.docs.desc.FactoryDesc;
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

    @Data
    @Accessors(chain = true, fluent = true)
    class Matcher implements DocsSource.Factory.Matcher {
        private String type;
        private String spType;
        private String desc;

        public Matcher(String type, String spType) {
            this.type = type;
            this.spType = spType;
        }

        @Override
        public FactoryDesc.MatcherDesc toDocs() {
            return new FactoryDesc.MatcherDesc(String.format("type: %s, sp: %s", type, spType))
                    .desc(desc);
        }
    }

    abstract class Base extends EntityFactory.Base<Long, MessageTemplate, MessageTemplateFactory.Criteria, MessageTemplateFactory.Matcher> implements MessageTemplateFactory {
    }

    @Extension
    class Default extends MessageTemplateFactory.Base {
        @Override
        public MessageTemplate create(Long id, MessageTemplateFactory.Criteria criteria) {
            return new MessageTemplate.Default(id);
        }

        @Override
        public MatcherSet<MessageTemplateFactory.Matcher, MessageTemplateFactory.Criteria> getMatcherSet() {
            return new MatcherSet.All<>();
        }

        @Override
        public FactoryDesc toDocs() {
            FactoryDesc docs = super.toDocs();
            return docs.value(docs.value() + "(default)");
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }
    }
}
