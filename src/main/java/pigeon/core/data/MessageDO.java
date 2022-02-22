package pigeon.core.data;

import lombok.Data;
import pigeon.core.entity.core.Message;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
public abstract class MessageDO implements DataObject<Long> {
    /**
     * 发送人
     */
    private String sender;
    /**
     * 推送目标
     */
    private String target;
    /**
     * 推送目标关联的用户 id（可空）
     */
    private String targetUserId;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 服务商类型
     */
    private String spType;
    /**
     * 服务商账号 id
     */
    private Long spAccountId;
    /**
     * 标题
     */
    private String title;
    /**
     * 正文
     */
    private String content;
    /**
     * 消息签名
     */
    private String signature;
    /**
     * 模板 id
     */
    private Long templateId;
    /**
     * 第三方模板 code
     */
    private String thirdTemplateCode;
    /**
     * 模板参数
     */
    private String params;
    /**
     * 投递 id（消息投递到第三方服务商进行发送时返回的 id）
     */
    private String deliveryId;
    /**
     * 标签
     */
    private String tag;
    /**
     * 消息状态
     */
    private Message.Status status;
    /**
     * 状态信息
     */
    private String statusRemark;
    /**
     * 所属消息集 id
     */
    private Long massId;
    /**
     * 所属消息子集 id
     */
    private Long subMassId;
    /**
     * 拓展内容（供插件实现时自行拓展使用）
     *
     * @since 0.2
     */
    private String ext;
}
