package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.domain.core.EventBus;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.RawMessageDeliverer;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.entity.core.holder.MessageDelivererHolder;
import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import com.github.taccisum.pigeon.core.repo.ThirdAccountRepo;
import com.google.common.collect.Lists;
import lombok.Setter;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
    protected void doDelivery() throws Exception {
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

    @Resource
    private EventBus eventBus;

    @Override
    public RawMessageDeliverer getMessageDeliverer() {
        // TODO::
        DummyDeliverer deliverer = new DummyDeliverer();
        deliverer.setEventBus(eventBus);
        deliverer.setDao(dao);
        return deliverer;
    }

    static class DummyDeliverer extends RawMessageDeliverer.Base {
        @Setter
        private MessageDAO dao;

        @Override
        public void deliver(MessageDO message) {
            // do nothing for this message type
            this.publish(new DeliveryEvent(Lists.newArrayList(message), 1, 0, 0));
        }

        @Override
        public void deliverBatchFast(List<MessageDO> messages) {
            // do nothing for this message type
            MessageDO o = new MessageDO();
            o.setStatus(Status.SENT);
            dao.updateBatchByIdList(o, messages.stream()
                    .map(MessageDO::getId)
                    .collect(Collectors.toList())
            );
            this.publish(new DeliveryEvent(messages, messages.size(), 0, 0));
        }
    }
}
