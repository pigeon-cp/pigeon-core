package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.sp.MailServiceProvider;

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
    protected MailServiceProvider getServiceProvider() {
        ServiceProvider sp = serviceProviderRepo.get(this.data().getSpType());
        if (sp instanceof MailServiceProvider) {
            return (MailServiceProvider) sp;
        }
        throw new DataErrorException("Mail.ServiceProvider", this.id(), "邮件消息可能关联了错误的服务提供商：" + sp.getType() + "，请检查数据是否异常");
    }
}
