package com.github.taccisum.pigeon.core.entity.core.template;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;

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
}
