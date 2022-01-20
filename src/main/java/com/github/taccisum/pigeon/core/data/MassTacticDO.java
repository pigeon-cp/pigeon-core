package com.github.taccisum.pigeon.core.data;

import com.github.taccisum.pigeon.core.entity.core.MassTactic;
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
     * 消息目标源
     */
    private String targetSource;
    /**
     * 目标源类型
     */
    private MassTactic.SourceType targetSourceType;
    /**
     * 准备好的数据集 id
     */
    private Long preparedMassId;
}
