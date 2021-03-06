package pigeon.core.entity.core.template;

import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.MessageTemplate;

/**
 * 短信模板
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/14
 */
public abstract class SMSTemplate extends MessageTemplate {
    public SMSTemplate(Long id) {
        super(id);
    }

    @Override
    public String getMessageType() {
        return Message.Type.SMS;
    }

    @Override
    protected String getAccountHeaderName() {
        return "phone";
    }
}
