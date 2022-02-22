package pigeon.core.repo;

import com.github.taccisum.domain.core.exception.DataNotFoundException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import org.springframework.stereotype.Component;
import pigeon.core.dao.MessageMassDAO;
import pigeon.core.data.MessageMassDO;
import pigeon.core.entity.core.MessageMass;

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
        return Optional.of(factory.createMessageMass(data.getId(), data.getType(), data.getSpType(), data.getMessageType()));
    }

    public MessageMass create(MessageMassDO data) {
        data.setStatus(MessageMass.Status.CREATING);
        Long id = dao.insert(data);
        return factory.createMessageMass(id, data.getType(), data.getSpType(), data.getMessageType());
    }

    @ErrorCode("MASS.NOT_FOUND")
    public static class NotFoundException extends DataNotFoundException {
        public NotFoundException(long id) {
            super("MessageMass", id);
        }
    }
}
