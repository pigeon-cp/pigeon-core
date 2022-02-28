package pigeon.core.entity.core;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import com.google.common.collect.Sets;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigeon.core.dao.MessageDAO;
import pigeon.core.dao.MessageTemplateDAO;
import pigeon.core.data.MessageDO;
import pigeon.core.data.MessageTemplateDO;
import pigeon.core.repo.MessageRepo;
import pigeon.core.repo.ServiceProviderRepo;
import pigeon.core.repo.ThirdAccountRepo;
import pigeon.core.repo.UserRepo;
import pigeon.core.utils.CSVUtils;
import pigeon.core.utils.JsonUtils;
import pigeon.core.utils.MagnitudeUtils;
import pigeon.core.valueobj.MessageInfo;
import pigeon.core.valueobj.PlaceHolderRule;
import pigeon.core.valueobj.Source;
import pigeon.core.valueobj.rule.ph.Direct;
import pigeon.core.valueobj.rule.ph.SimpleIndex;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    MessageDAO messageDAO;
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
     * @param sender 发送人地址
     * @param target 消息目标
     * @param params 模板参数
     * @return 新建的消息
     * @throws MessageRepo.CreateMessageException 消息创建失败
     */
    public Message initMessage(String sender, String target, Object params) throws MessageRepo.CreateMessageException {
        return this.initMessage(sender, new User.Dummy(target), params);
    }

    /**
     * @param sender 发送人地址
     * @param user   消息目标用户
     * @param params 模板参数
     * @return 新建的消息
     * @throws MessageRepo.CreateMessageException 消息创建失败
     */
    public Message initMessage(String sender, User user, Object params) throws MessageRepo.CreateMessageException {
        return this.initMessage(sender, user, params, null, null);
    }

    /**
     * @param sender    发送人地址
     * @param target    消息目标
     * @param params    模板参数
     * @param signature 消息签名
     * @param ext       自定义拓展参数
     * @return 新建的消息
     * @throws MessageRepo.CreateMessageException 消息创建失败
     */
    public Message initMessage(String sender, String target, Object params, String signature, String ext) throws MessageRepo.CreateMessageException {
        return this.initMessage(sender, new User.Dummy(target), params, signature, ext);
    }

    /**
     * 使用当前模板创建出一条新的待发送消息实例
     *
     * @param sender    发送人地址
     * @param user      消息目标用户
     * @param params    模板参数
     * @param signature 签名
     * @param ext       扩展参数
     * @return 新建的消息
     * @throws MessageRepo.CreateMessageException 消息创建失败
     */
    public Message initMessage(String sender, User user, Object params, String signature, String ext) throws MessageRepo.CreateMessageException {
        return messageRepo.create(initMessageInMemory(sender, user, params, signature, ext));
    }

    /**
     * @param sender 发送人地址
     * @param target 消息目标
     * @param params 模板参数
     * @return 构建的消息数据对象
     * @throws InitMessageException 通过模板创建消息失败
     * @since 0.2
     */
    public MessageDO initMessageInMemory(String sender, String target, Object params) throws InitMessageException {
        return this.initMessageInMemory(sender, new User.Dummy(target), params);
    }

    /**
     * @param sender 发送人地址
     * @param user   消息目标用户
     * @param params 模板参数
     * @return 构建的消息数据对象
     * @throws InitMessageException 通过模板创建消息失败
     * @since 0.2
     */
    public MessageDO initMessageInMemory(String sender, User user, Object params) throws InitMessageException {
        return this.initMessageInMemory(sender, user, params, null, null);
    }

    /**
     * @param sender    发送人地址
     * @param target    消息目标
     * @param params    模板参数
     * @param signature 消息签名
     * @param ext       自定义拓展参数
     * @return 构建的消息数据对象
     * @throws InitMessageException 通过模板创建消息失败
     * @since 0.2
     */
    public MessageDO initMessageInMemory(String sender, String target, Object params, String signature, String ext) throws InitMessageException {
        return this.initMessageInMemory(sender, new User.Dummy(target), params, signature, ext);
    }

    /**
     * <pre>
     * 使用当前模板创建出一条新的仅存在于内存中的待发送消息数据对象
     *
     * 例如在大规模、批量创建消息时你可以使用此方法先将数据存储在内存中，然后再通过批量插入到 DB 以提高性能
     * </pre>
     *
     * @param sender    发送人地址
     * @param user      消息目标用户
     * @param params    模板参数
     * @param signature 消息签名
     * @param ext       自定义拓展参数
     * @return 构建的消息数据对象
     * @throws InitMessageException 通过模板创建消息失败
     * @since 0.2
     */
    public MessageDO initMessageInMemory(String sender, User user, Object params, String signature, String ext) throws InitMessageException {
        MessageTemplateDO data = this.data();
        MessageDO o = messageDAO.newEmptyDataObject();
        o.setType(this.getMessageType());
        o.setSpType(data.getSpType());
        o.setSpAccountId(data.getSpAccountId());
        o.setThirdTemplateCode(data.getThirdCode());
        o.setSignature(signature);
        o.setSender(sender);
        o.setTarget(user.getAccountFor(this));
        o.setTargetUserId(user.getId());
        o.setTemplateId(this.id());
        if (params instanceof String) {
            o.setParams((String) params);
        } else {
            o.setParams(JsonUtils.stringify(params));
        }

        PlaceHolderRule rule = this.getPlaceHolderRule(data.getPlaceholderRule());
        o.setTitle(data.getTitle());
        o.setContent(rule.resolve(data.getContent(), o.getParams()));

        o.setTag(data.getTag());
        o.setExt(ext);
        return o;
    }

    /**
     * 获取模板占位符规则
     *
     * @param name 规则名称
     * @return 占位符规则具体实现
     */
    protected PlaceHolderRule getPlaceHolderRule(String name) {
        Set<String> directKeys = Sets.newHashSet("DIRECT", "NONE", "REMOTE");
        if (StringUtils.isBlank(name) || directKeys.contains(name.toUpperCase().trim())) {
            return new Direct();
        }

        if (name.startsWith("LOCAL:SIMPLE")) {
            // 本地处理
            return new SimpleIndex();
        }
        log.warn("无法识别此占位符规则 {}，将返回默认规则", name);
        return new Direct();
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
     *
     * @return 模板关联的三方账号
     */
    public ThirdAccount getSpAccount() {
        try {
            return this.getServiceProvider()
                    .getAccountOrThrow(this.data().getSpAccountId());
        } catch (ThirdAccountRepo.NotFoundException e) {
            throw new DataErrorException("消息模板", this.id(), "关联的三方账号不存在");
        }
    }

    /**
     * 获取模板关联的消息类型
     *
     * @return 模板关联的消息类型
     */
    public abstract String getMessageType();

    /**
     * @param source 目标源
     * @return 解析的信息 list
     * @throws ResolveSourceException 通过模板解析消息源失败
     */
    public List<MessageInfo> resolve(Source source) throws ResolveSourceException {
        return this.resolve(source, new MessageInfo());
    }

    /**
     * @param source 目标源
     * @param def    缺省值
     * @return 解析的信息 list
     * @throws ResolveSourceException 通过模板解析消息源失败
     */
    public List<MessageInfo> resolve(Source source, MessageInfo def) throws ResolveSourceException {
        return this.resolve(0, Integer.MAX_VALUE, source, def);
    }

    /**
     * 解析群发数据源为消息目标实体
     *
     * @param start  起始行（包含）
     * @param end    结束行（不包含）
     * @param source 目标源
     * @param def    缺省值（充当数据源中缺失的信息默认值）
     * @return 解析的信息 list
     * @throws ResolveSourceException 通过模板解析消息源失败
     */
    public List<MessageInfo> resolve(Integer start, Integer end, Source source, MessageInfo def) throws ResolveSourceException {
        List<MessageInfo> targets = new ArrayList<>();
        Timer timer = Timer.builder("template.source.resolve")
                .description("通过模板解析目标源")
                .tag("method", "CSV")
                .tag("type", this.getClass().getName())
                .tag("source.type", source.getClass().getName())
                .tag("start", MagnitudeUtils.fromInt(start).name())
                .tag("size", MagnitudeUtils.fromInt(end - start).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        timer.record(() -> {
            try {
                CSVParser csv = CSVFormat.DEFAULT.builder()
                        .setHeader().setSkipHeaderRecord(true)
                        .build().parse(new BufferedReader(new InputStreamReader(source.getInputStream())));
                for (CSVRecord row : csv) {
                    // 不计入 header，真正的 line num 应该 - 1
                    long lineNum = row.getRecordNumber() - 1;
                    if (lineNum >= start && lineNum < end) {
                        MessageInfo target = this.map(row, def);
                        if (target != null) {
                            targets.add(target);
                        } else {
                            log.warn("第 {} 行： {} 解析失败", lineNum, row);
                        }
                    }
                }
            } catch (IOException e) {
                throw new ResolveSourceException("解析目标源发生 I/O 异常", e);
            } catch (Exception e) {
                throw new ResolveSourceException("解析目标源发生错误", e);
            }
        });
        return targets;
    }

    /**
     * 通过此模板解析 CSV 行数据为 {@link MessageInfo} 对象
     *
     * @param row 行数据
     * @param def 缺省值
     * @return 解析结果
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

        // TODO:: refactor, 通过 info 的字段去自动解析模板，提高拓展性
        info.setSender(CSVUtils.getOrDefault(row, "sender", def.getSender()));
        info.setParams(CSVUtils.getOrDefault(row, "params", def.getParams()));
        info.setSignature(CSVUtils.getOrDefault(row, "sign", def.getSignature()));
        info.setExt(CSVUtils.getOrDefault(row, "ext", def.getExt()));

        return info;
    }

    /**
     * TODO:: rename
     *
     * @return account 列对应的 header 名
     * @deprecated 后续可能会移除
     */
    protected abstract String getAccountHeaderName();

    @ErrorCode(value = "TEMPLATE.RESOLVE_TARGET_SOURCE", description = "解析目标源失败")
    public static class ResolveSourceException extends DomainException {
        public ResolveSourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ErrorCode(value = "TEMPLATE.INIT_MESSAGE", description = "通过模板创建消息失败")
    public static class InitMessageException extends DomainException {
        public InitMessageException(String reason) {
            super(String.format("通过模板创建消息失败，原因：%s", reason));
        }
    }

    public static class Default extends MessageTemplate {
        public Default(Long id) {
            super(id);
        }

        @Override
        public String getMessageType() {
            return this.data().getType();
        }

        @Override
        protected String getAccountHeaderName() {
            return "target";
        }
    }
}
