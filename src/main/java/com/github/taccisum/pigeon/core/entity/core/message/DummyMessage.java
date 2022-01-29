package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import com.github.taccisum.pigeon.core.repo.ThirdAccountRepo;

/**
 * 不做任何分发动作的消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class DummyMessage extends Message {
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
}
