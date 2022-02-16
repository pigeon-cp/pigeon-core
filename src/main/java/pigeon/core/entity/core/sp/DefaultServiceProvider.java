package pigeon.core.entity.core.sp;

import pigeon.core.entity.core.ServiceProvider;
import pigeon.core.entity.core.ThirdAccount;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class DefaultServiceProvider extends ServiceProvider.Base implements
        MailServiceProvider,
        MessageServiceProvider,
        SMSServiceProvider {
    public DefaultServiceProvider(String id) {
        super(id);
    }

    @Override
    protected boolean match(ThirdAccount account) {
        return true;
    }
}
