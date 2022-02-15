package com.github.taccisum.pigeon.core.entity.core.sp.account;

import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.data.ThirdAccountDO;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.entity.core.message.Mail;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

/**
 * 邮件服务器账号
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class MailServerAccount extends ThirdAccount {
    private JavaMailSender javaMailSender;

    public MailServerAccount(long id) {
        super(id);
    }

    /**
     * 发送邮件
     */
    public void send(Mail mail) {
        MessageDO data = mail.data();
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(data.getSender());
        String[][] targets = resolveTargets(data.getTarget());
        String to = targets[0][0];
        if (StringUtils.isBlank(to)) {
            throw new IllegalArgumentException("to can not be null or empty");
        }
        msg.setTo(to);
        msg.setCc(targets[1]);
        msg.setBcc(targets[2]);
        msg.setSubject(data.getTitle());
        msg.setText(data.getContent());
        msg.setSentDate(new Date());
        msg.setReplyTo(data.getSender());
        this.getSender().send(msg);
    }

    static String[][] resolveTargets(String targets) {
        String to = null;
        String[] cc = null;
        String[] bcc = null;
        for (String target : targets.split("[;；]")) {
            if (StringUtils.isBlank(target)) {
                continue;
            }

            String[] t = target.split("[:：]");

            if (t.length == 1) {
                to = t[0];
            } else {
                String key = t[0];
                String val = t[1];
                if ("to".equalsIgnoreCase(key)) {
                    to = val;
                } else if ("cc".equalsIgnoreCase(key)) {
                    cc = val.split("[,，]");
                } else if ("bcc".equalsIgnoreCase(key)) {
                    bcc = val.split("[,，]");
                } else {
                    // ignore
                }
            }
        }

        return new String[][]{
                new String[]{to},
                cc,
                bcc
        };
    }

    public JavaMailSender getSender() {
        if (javaMailSender == null) {
            ThirdAccountDO data = this.data();
            JavaMailSenderImpl impl = new JavaMailSenderImpl();
            impl.setUsername(data.getAppId());
            impl.setPassword(data.getAppSecret());
            URI uri;
            try {
                uri = new URI(data.getExt());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(data.getExt());
            }
            impl.setHost(uri.getHost());
            impl.setPort(uri.getPort() == -1 ? 465 : uri.getPort());
            impl.setProtocol(Optional.ofNullable(uri.getScheme()).orElse("smtps"));
            javaMailSender = impl;
        }
        return javaMailSender;
    }
}
