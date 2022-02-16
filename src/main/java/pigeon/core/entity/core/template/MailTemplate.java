package pigeon.core.entity.core.template;

import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.MessageTemplate;

/**
 * 邮件模板
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/14
 */
public abstract class MailTemplate extends MessageTemplate {
    public MailTemplate(Long id) {
        super(id);
    }

    @Override
    public String getMessageType() {
        return Message.Type.MAIL;
    }

    @Override
    protected String getAccountHeaderName() {
        return "mail";
    }
}
