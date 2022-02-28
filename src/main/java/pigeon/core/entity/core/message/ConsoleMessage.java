package pigeon.core.entity.core.message;

import pigeon.core.data.MessageDO;
import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.ThirdAccount;
import pigeon.core.entity.core.sp.MessageServiceProvider;
import pigeon.core.repo.ThirdAccountRepo;

/**
 * 通过控制台打印的消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class ConsoleMessage extends Message {
    public ConsoleMessage(Long id) {
        super(id);
    }

    @Override
    public boolean isRealTime() {
        return true;
    }

    @Override
    protected void doDelivery() {
        MessageDO data = this.data();
        System.out.printf("To %s.\n%s\n%s\n  - by %s",
                data.getTarget(),
                data.getTitle(),
                data.getContent(),
                data.getSender()
        );
    }

    @Override
    public MessageServiceProvider getServiceProvider() {
        return null;
    }

    @Override
    public ThirdAccount getSpAccount() throws ThirdAccountRepo.NotFoundException {
        return null;
    }
}
