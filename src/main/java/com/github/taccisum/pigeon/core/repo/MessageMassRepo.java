package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MessageMassRepo {
    @Resource
    private MessageMassDAO dao;
    @Resource
    private Factory factory;

    public Optional<MessageMass> get(Long id) {
        MessageMassDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createMessageMass(data.getId()));
    }

    public MessageMass create(MessageMassDO data) {
        data.setStatus(MessageMass.Status.CREATING);
        Long id = dao.insert(data);
        return factory.createMessageMass(id);
    }
}
