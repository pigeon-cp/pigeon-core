package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.dao.MessageTemplateDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.data.MessageTemplateDO;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.ServiceProviderRepo;
import com.github.taccisum.pigeon.core.repo.UserRepo;
import com.github.taccisum.pigeon.core.utils.CSVUtils;
import com.github.taccisum.pigeon.core.utils.JsonUtils;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import com.github.taccisum.pigeon.core.valueobj.Source;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息模板，可以是短信模板、邮件模板等等
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MessageTemplate extends Entity.Base<Long> {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private MessageTemplateDAO dao;
    @Resource
    protected UserRepo userRepo;
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
    public Message initMessage(String sender, String target, Object params) throws MessageRepo.CreateMessageException {
        return initMessage(sender, new User.Dummy(target), params);
    }

    /**
     * 使用当前模板创建出一条新的待发送消息实例
     *
     * @param sender 发送人地址
     * @param user   消息目标用户
     * @param params 模板参数
     */
    public Message initMessage(String sender, User user, Object params) throws MessageRepo.CreateMessageException {
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
    public abstract String getMessageType();

    public List<MessageInfo> resolve(Source source) throws ResolveSourceException {
        return this.resolve(source, new MessageInfo());
    }

    /**
     * 解析群发数据源为消息目标实体
     *
     * @param source 目标源
     * @param def    缺省值（充当数据源中缺失的信息默认值）
     */
    public List<MessageInfo> resolve(Source source, MessageInfo def) throws ResolveSourceException {
        List<MessageInfo> targets = new ArrayList<>();
        try {
            CSVParser csv = CSVFormat.DEFAULT.builder()
                    .setHeader().setSkipHeaderRecord(true)
                    .build().parse(new BufferedReader(new InputStreamReader(source.getInputStream())));
            for (CSVRecord row : csv) {
                MessageInfo target = this.map(row, def);
                if (target != null) {
                    targets.add(target);
                } else {
                    log.warn("第 {} 行： {} 解析失败", csv.getRecordNumber(), row);
                }
            }
        } catch (IOException e) {
            throw new ResolveSourceException("解析目标源发生 I/O 异常", e);
        } catch (Exception e) {
            throw new ResolveSourceException("解析目标源发生错误", e);
        }
        return targets;
    }

    /**
     * 解析行数据为目标实体
     *
     * @param row 行数据
     * @param def 缺省值
     */
    protected MessageInfo map(CSVRecord row, MessageInfo def) {
        MessageInfo info = new MessageInfo();

        String mail = CSVUtils.getOrDefault(row, this.getAccountHeaderName(), 0, null);

        if (StringUtils.isEmpty(mail)) {
            return null;
        }
        if (mail.startsWith("u\\_")) {
            info.setAccount(this.userRepo.get(mail.substring(2, mail.length()))
                    .orElse(null));
        } else {
            info.setAccount(mail);
        }

        info.setSender(CSVUtils.getOrDefault(row, "sender", def.getSender()));
        info.setParams(CSVUtils.getOrDefault(row, "params", def.getParams()));

        return info;
    }

    protected abstract String getAccountHeaderName();

    /**
     * 解析目标源异常
     */
    public static class ResolveSourceException extends DomainException {
        public ResolveSourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
