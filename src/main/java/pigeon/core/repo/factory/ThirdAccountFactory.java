package pigeon.core.repo.factory;

import pigeon.core.entity.core.ThirdAccount;
import pigeon.core.entity.core.sp.account.MailServerAccount;
import pigeon.core.repo.EntityFactory;
import lombok.Data;
import org.pf4j.Extension;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ThirdAccountFactory extends EntityFactory<Long, ThirdAccount, ThirdAccountFactory.Criteria> {
    @Data
    class Criteria {
        String username;
        String type;
        String spType;

        public Criteria(String username, String type, String spType) {
            this.username = username;
            this.type = type;
            this.spType = spType;
        }
    }

    /**
     * @since 0.2
     */
    @Extension
    class Default implements ThirdAccountFactory {
        @Override
        public ThirdAccount create(Long id, Criteria criteria) {
            switch (criteria.getType()) {
                case "SMTP_SERVER":
                case "IMAP_SERVER":
                case "MAIL_SERVER":
                    return new MailServerAccount(id);
                default:
                    throw new UnsupportedOperationException(criteria.getType());
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
