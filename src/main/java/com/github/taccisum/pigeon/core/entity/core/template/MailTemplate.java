package com.github.taccisum.pigeon.core.entity.core.template;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTarget;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import com.github.taccisum.pigeon.core.repo.UserRepo;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Optional;

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
    protected MessageTarget map(CSVRecord row) {
        String mail = Optional.ofNullable(row.get("mail"))
                .orElse(row.get(0));

        if (StringUtils.isEmpty(mail)) {
            return null;
        }

        if (mail.startsWith("u\\_")) {
            return userRepo.get(mail.substring(2, mail.length()))
                    .orElse(null);
        }

        return new MessageTarget.Default(mail);
    }
}
