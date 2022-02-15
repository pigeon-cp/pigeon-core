package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.sp.MailServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.sp.account.MailServerAccount;

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
        protected void doDelivery() throws Exception {
            MailServerAccount account = (MailServerAccount) this.getServiceProvider().getAccountOrThrow(this.data().getSpAccountId());
            account.send(this);
        }
    }
}
