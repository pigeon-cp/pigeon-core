package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.entity.core.template.MailTemplate;
import com.github.taccisum.pigeon.core.entity.core.template.SMSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/17
 */
public abstract class User extends Entity.Base<String> {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public User(String id) {
        super(id);
    }

    /**
     * 获取用户手机号码
     */
    public abstract String getPhoneNum();

    /**
     * 获取用户邮箱账号
     */
    public abstract String getMailAccount();

    /**
     * <pre>
     * 获取适用于消息模板的账号，例如：
     *
     * - 模板为短信模板 {@link SMSTemplate} 或者类型为 {@link Message.Type#SMS}，则返回手机号码
     * - 模板为邮件模板 {@link MailTemplate} 或者类型为 {@link Message.Type#MAIL}，则返回邮箱账号
     * </pre>
     */
    public String getAccountFor(MessageTemplate template) {
        String account = null;
        // 优先判断类型
        if (template instanceof MailTemplate) {
            account = this.getMailAccount();
        } else if (template instanceof SMSTemplate) {
            account = this.getPhoneNum();
        }

        if (account == null) {
            // 其次判断消息 type
            switch (template.getMessageType()) {
                case Message.Type.MAIL:
                    account = this.getMailAccount();
                    break;
                case Message.Type.SMS:
                    account = this.getPhoneNum();
                    break;
                default:
                    break;
            }
        }
        if (account == null) {
            log.warn("未找到用户 {} 适用于消息模板 {} 的账号，将返回 null", this.id(), template.id());
        }
        return account;
    }

    public static class Dummy extends User {
        private String account;

        public Dummy(String account) {
            super(null);
            this.account = account;
        }

        @Override
        public String getPhoneNum() {
            return account;
        }

        @Override
        public String getMailAccount() {
            return account;
        }

        @Override
        public String getAccountFor(MessageTemplate template) {
            return account;
        }
    }
}
