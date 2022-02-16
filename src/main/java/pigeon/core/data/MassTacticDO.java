package pigeon.core.data;

import pigeon.core.entity.core.MassTactic;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
public abstract class MassTacticDO implements DataObject<Long> {
    /**
     * 策略类型
     */
    private String type;
    /**
     * 策略状态
     */
    private MassTactic.Status status;
    /**
     * 是否测试通过（null: 未测试，true/false: 测试通过/不通过）
     */
    private Boolean hasTest;
    /**
     * 是否必须测试通过才能执行
     */
    private Boolean mustTest;
    /**
     * 执行次数
     */
    private Integer execTimes;
    /**
     * 群发时使用的模板 id
     */
    private Long templateId;
    /**
     * 缺省的消息发送人
     */
    private String defaultSender;
    /**
     * 缺省的模板参数
     */
    private String defaultParams;
    /**
     * 缺省的消息签名
     *
     * @since 0.2
     */
    private String defaultSignature;
    /**
     * 缺省的拓展参数内容
     *
     * @since 0.2
     */
    private String defaultExt;
    /**
     * 数据源（应包含发送目标，可包括模板参数等其它配置，取决于具体模板实现）
     */
    private String source;
    /**
     * 数据源 size（冗余）
     */
    private Integer sourceSize;
    /**
     * 数据源类型
     */
    private MassTactic.SourceType sourceType;
    /**
     * 准备好的数据集 id
     */
    private Long preparedMassId;
}
