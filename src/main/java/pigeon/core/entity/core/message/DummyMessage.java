package pigeon.core.entity.core.message;

import pigeon.core.data.MessageDO;
import pigeon.core.entity.core.Message;
import pigeon.core.entity.core.RawMessageDeliverer;
import pigeon.core.entity.core.ThirdAccount;
import pigeon.core.entity.core.holder.MessageDelivererHolder;
import pigeon.core.entity.core.sp.MessageServiceProvider;
import pigeon.core.repo.ThirdAccountRepo;

import java.util.List;

/**
 * 不做任何分发动作的消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class DummyMessage extends Message implements MessageDelivererHolder {
    public DummyMessage(Long id) {
        super(id);
    }

    @Override
    public boolean isRealTime() {
        return true;
    }

    @Override
    protected void doDelivery() {
        // do nothing for this message type
    }

    @Override
    public MessageServiceProvider getServiceProvider() {
        return null;
    }

    @Override
    public ThirdAccount getSpAccount() throws ThirdAccountRepo.NotFoundException {
        return null;
    }

    @Override
    public RawMessageDeliverer getMessageDeliverer() {
        return new DummyDeliverer();
    }

    static class DummyDeliverer implements RawMessageDeliverer {
        @Override
        public String deliver(MessageDO message) {
            // do nothing for this message type
            return null;
        }

        @Override
        public String deliverBatchFast(List<MessageDO> messages) {
            // do nothing for this message type
            return null;
        }
    }
}
