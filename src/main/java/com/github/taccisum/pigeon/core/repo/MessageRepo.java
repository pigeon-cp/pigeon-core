package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MessageRepo {
    @Autowired
    private MessageDAO mapper;
    @Autowired
    private Factory factory;

    public Message create(MessageDO data) {
        mapper.insert(data);
        return factory.createMessage(data.getId(), data.getType(), data.getSpType());
    }
}
