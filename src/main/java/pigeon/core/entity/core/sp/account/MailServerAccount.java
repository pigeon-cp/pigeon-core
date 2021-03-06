package pigeon.core.entity.core.sp.account;

import org.apache.commons.lang.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import pigeon.core.data.MessageDO;
import pigeon.core.data.ThirdAccountDO;
import pigeon.core.entity.core.ThirdAccount;
import pigeon.core.entity.core.message.Mail;

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
     *
     * @param mail 邮件消息
     */
    public void send(Mail mail) {
        MessageDO data = mail.data();
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(data.getSender());
        String[][] targets = mail.getTargets();
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

    private JavaMailSender getSender() {
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
