package pigeon.core.entity.core.message;

import com.github.taccisum.domain.core.exception.DataErrorException;
import org.apache.commons.lang.StringUtils;
import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.ServiceProvider;
import pigeon.core.entity.core.sp.MailServiceProvider;
import pigeon.core.entity.core.sp.account.MailServerAccount;

/**
 * 邮件消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class Mail extends Message {
    public Mail(Long id) {
        super(id);
    }

    @Override
    public MailServiceProvider getServiceProvider() {
        ServiceProvider sp = serviceProviderRepo.get(this.data().getSpType());
        if (sp instanceof MailServiceProvider) {
            return (MailServiceProvider) sp;
        }
        throw new DataErrorException("Mail.ServiceProvider", this.id(), "邮件消息可能关联了错误的服务提供商：" + sp.getType() + "，请检查数据是否异常");
    }

    /**
     * 获取邮件发送目标
     *
     * @return arr, to:arr[0], cc:arr[1], bcc:arr[2]
     */
    public String[][] getTargets() {
        return resolveTargets(this.data().getTarget());
    }

    public String getTo() {
        return this.getTargets()[0][0];
    }

    public String[] getCc() {
        return this.getTargets()[1];
    }

    public String[] getBcc() {
        return this.getTargets()[2];
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


    /**
     * 使用标准 POP3/SMTP 协议的默认实现
     *
     * @since 0.2
     */
    public static class Default extends Mail {
        public Default(Long id) {
            super(id);
        }

        @Override
        public boolean isRealTime() {
            return false;
        }

        @Override
        protected void doDelivery() {
            MailServerAccount account = (MailServerAccount) this.getServiceProvider().getAccountOrThrow(this.data().getSpAccountId());
            account.send(this);
        }
    }
}
