package pigeon.core.repo;

import com.github.taccisum.domain.core.exception.DataNotFoundException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pigeon.core.dao.MessageTemplateDAO;
import pigeon.core.data.MessageTemplateDO;
import pigeon.core.entity.core.MessageTemplate;

import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MessageTemplateRepo {
    @Autowired
    private MessageTemplateDAO mapper;
    @Autowired
    private Factory factory;

    public MessageTemplate create(MessageTemplateDO data) {
        mapper.insert(data);
        return factory.createMessageTemplate(data.getId(), data.getType(), data.getSpType());
    }

    public Optional<MessageTemplate> get(long id) {
        MessageTemplateDO data = mapper.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createMessageTemplate(data.getId(), data.getType(), data.getSpType()));
    }

    public MessageTemplate getOrThrow(long id) throws MessageTemplateNotFoundException {
        return this.get(id)
                .orElseThrow(() -> new MessageTemplateNotFoundException(id));
    }

    @ErrorCode(value = "TEMPLATE", inherited = true, description = "消息模板不存在")
    public static class MessageTemplateNotFoundException extends DataNotFoundException {
        public MessageTemplateNotFoundException(long id) {
            super("消息模板", id);
        }
    }
}
