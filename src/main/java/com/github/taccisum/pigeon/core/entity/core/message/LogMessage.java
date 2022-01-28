package com.github.taccisum.pigeon.core.entity.core.message;

import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import com.github.taccisum.pigeon.core.repo.ThirdAccountRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过日志框架打印的消息
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class LogMessage extends Message {
    private Logger log = LoggerFactory.getLogger("LogMessage");

    public LogMessage(Long id) {
        super(id);
    }

    @Override
    public boolean isRealTime() {
        return true;
    }

    @Override
    protected void doDelivery() throws Exception {
        MessageDO data = this.data();
        log.info("To {}.\n{}\n{}\n  - by {}",
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
