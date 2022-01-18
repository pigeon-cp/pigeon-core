package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.dao.MessageTemplateDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.data.MessageTemplateDO;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.ServiceProviderRepo;
import com.github.taccisum.pigeon.core.utils.JsonUtils;

import javax.annotation.Resource;

/**
 * 消息模板，可以是短信模板、邮件模板等等
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MessageTemplate extends Entity.Base<Long> {
    @Resource
    private MessageTemplateDAO dao;
    @Resource
    protected MessageRepo messageRepo;
    @Resource
    private ServiceProviderRepo serviceProviderRepo;

    public MessageTemplate(Long id) {
        super(id);
    }

    public MessageTemplateDO data() {
        return dao.selectById(this.id());
    }

    /**
     * 使用当前模板创建出一条新的待发送消息实例
     *
     * @param sender 发送人地址
     * @param target 消息目标
     * @param params 模板参数
     */
    public Message initMessage(String sender, String target, Object params) {
        return initMessage(sender, new User.Dummy(target), params);
    }

    /**
     * 使用当前模板创建出一条新的待发送消息实例
     *
     * @param sender 发送人地址
     * @param user   消息目标用户
     * @param params 模板参数
     */
    public Message initMessage(String sender, User user, Object params) {
        MessageTemplateDO data = this.data();
        MessageDO o = new MessageDO();
        o.setType(this.getMessageType());
        o.setSpType(data.getSpType());
        o.setSpAccountId(data.getSpAccountId());
        o.setSender(sender);
        o.setTarget(user.getAccountFor(this));
        o.setTargetUserId(user.getId());
        o.setTemplateId(this.id());
        o.setParams(JsonUtils.stringify(params));
        o.setTitle(data.getTitle());
        o.setContent(data.getContent());
        o.setTag(data.getTag());
        return messageRepo.create(o);
    }

    /**
     * 获取模板关联的服务商
     *
     * @return 服务商实体
     */
    protected ServiceProvider getServiceProvider() {
        return this.serviceProviderRepo.get(this.data().getSpType());
    }

    /**
     * 获取模板关联的服务商账号
     */
    public ThirdAccount getSpAccount() {
        return this.getServiceProvider()
                .getAccountOrThrow(this.data().getSpAccountId());
    }

    /**
     * 获取模板关联的消息类型
     */
    protected abstract String getMessageType();
}
