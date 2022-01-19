package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

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
    @Resource
    MessageTemplateRepo messageTemplateRepo;

    /**
     * 创建一条新的消息实体（初始状态默认为 {@link  Message.Status#NOT_SEND}）
     */
    public Message create(MessageDO data) throws CreateMessageException {
        data.setStatus(Message.Status.NOT_SEND);

        mapper.insert(data);
        Message message = factory.createMessage(data.getId(), data.getType(), data.getSpType());

        // 校验模板
        if (message.shouldRelateTemplate()) {
            if (data.getTemplateId() == null) {
                throw new CreateMessageException("此消息必须指定模板");
            }
            MessageTemplate template = messageTemplateRepo.getOrThrow(data.getTemplateId());
            if (!Objects.equals(template.getMessageType(), data.getType())) {
                throw new CreateMessageException(String.format("模板 %d 类型与消息不匹配", template.id()));
            }
        }
        // 校验服务商
        try {
            message.getSpAccount();
        } catch (ThirdAccountRepo.NotFoundException e) {
            throw new CreateMessageException(e.getMessage());
        }

        return message;
    }

    /**
     * 消息创建异常
     */
    public static class CreateMessageException extends DomainException {
        public CreateMessageException(String reason) {
            super("消息创建异常，原因：%s", reason);
        }

        public CreateMessageException(String message, Object... args) {
            super(message, args);
        }
    }
}
