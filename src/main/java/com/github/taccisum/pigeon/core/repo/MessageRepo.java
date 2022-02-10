package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MessageRepo {
    @Resource
    private MessageDAO dao;
    @Resource
    private Factory factory;
    @Resource
    MessageTemplateRepo messageTemplateRepo;

    /**
     * 根据关联的第三方消息 id 查找消息实体
     *
     * @param id 第三方消息 id
     */
    public Optional<Message> getByThirdId(String id) {
        throw new NotImplementedException();
    }

    /**
     * 创建一条新的消息实体（初始状态默认为 {@link  Message.Status#NOT_SEND}）
     */
    public Message create(MessageDO data) throws CreateMessageException {
        data.setStatus(Message.Status.NOT_SEND);
        if (StringUtils.isBlank(data.getSender())) {
            data.setSender(Message.DEFAULT_SENDER);
        }

        dao.insert(data);
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

    public List<Message> listByMassId(Long massId, long limit) {
        return this.dao.selectListByMassId(massId, limit)
                .stream()
                .map(data -> factory.createMessage(data.getId(), data.getType(), data.getSpType()))
                .collect(Collectors.toList());
    }

    public List<Message> listBySubMassId(Long subMassId) {
        return this.toEntities(this.dao.selectListBySubMassId(subMassId));
    }

    private List<Message> toEntities(List<MessageDO> ls) {
        if (CollectionUtils.isEmpty(ls)) {
            return new ArrayList<>();
        }
        return ls
                .stream()
                .map(data -> {
                    Message message = factory.createMessage(data.getId(), data.getType(), data.getSpType());
                    message.setData(data);
                    return message;
                })
                .collect(Collectors.toList());
    }

    /**
     * 消息创建异常
     */
    @ErrorCode(value = "MESSAGE_CREATION", description = "消息创建错误")
    public static class CreateMessageException extends DomainException {
        public CreateMessageException(String reason) {
            super("消息创建异常，原因：%s", reason);
        }

        public CreateMessageException(String message, Object... args) {
            super(message, args);
        }
    }
}
