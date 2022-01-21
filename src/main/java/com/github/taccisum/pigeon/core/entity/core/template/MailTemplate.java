package com.github.taccisum.pigeon.core.entity.core.template;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import com.github.taccisum.pigeon.core.repo.UserRepo;
import com.github.taccisum.pigeon.core.utils.CSVUtils;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

/**
 * 邮件模板
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/14
 */
public abstract class MailTemplate extends MessageTemplate {
    @Resource
    private UserRepo userRepo;

    public MailTemplate(Long id) {
        super(id);
    }

    @Override
    public String getMessageType() {
        return Message.Type.MAIL;
    }

    @Override
    protected MessageInfo map(CSVRecord row, MessageInfo def) {
        MessageInfo info = new MessageInfo();

        String mail = CSVUtils.getOrDefault(row, "mail", 0, null);

        if (StringUtils.isEmpty(mail)) {
            return null;
        }
        if (mail.startsWith("u\\_")) {
            info.setAccount(userRepo.get(mail.substring(2, mail.length()))
                    .orElse(null));
        } else {
            info.setAccount(mail);
        }

        info.setSender(CSVUtils.getOrDefault(row, "sender", def.getSender()));
        info.setParams(CSVUtils.getOrDefault(row, "params", def.getParams()));

        return info;
    }
}
