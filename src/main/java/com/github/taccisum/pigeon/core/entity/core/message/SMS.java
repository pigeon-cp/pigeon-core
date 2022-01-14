package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.sp.SMSServiceProvider;

/**
 * 短信消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class SMS extends Message {
    public SMS(Long id) {
        super(id);
    }

    @Override
    protected SMSServiceProvider getServiceProvider() {
        ServiceProvider sp = this.serviceProviderRepo.get(this.data().getSpType());
        if (sp instanceof SMSServiceProvider) {
            return (SMSServiceProvider) sp;
        }
        throw new DataErrorException("SMS.ServiceProvider", this.id(), "短信消息可能关联了错误的服务提供商：" + sp.getType() + "，请检查数据是否异常");
    }
}
