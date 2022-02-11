package com.github.taccisum.pigeon.core.data;

import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
public abstract class MessageMassDO implements DataObject<Long> {
    @Override
    public abstract Long getId();

    @Override
    public abstract void setId(Long id);

    /**
     * 消息集类型
     */
    private String type;
    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 群发服务商类型
     */
    private String spType;
    /**
     * 是否测试集
     */
    private Boolean test;
    /**
     * 集合状态
     */
    private MessageMass.Status status;
    /**
     * 所属群发策略 id
     */
    private Long tacticId;
    /**
     * 集合 size
     */
    private Integer size;
    /**
     * 推送成功数量
     */
    private Integer successCount;
    /**
     * 推送失败数量
     */
    private Integer failCount;
    /**
     * 推送错误数量
     */
    private Integer errorCount;
}
