package com.github.taccisum.pigeon.core.data;

import com.github.taccisum.pigeon.core.entity.core.SubMass;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Data
public abstract class SubMassDO implements DataObject<Long> {
    /**
     * 此子集所属消息集合 id
     */
    private Long mainId;
    /**
     * 子集合状态
     */
    private SubMass.Status status;
    /**
     * 投递 id
     *
     * @since 0.2
     */
    private String deliveryId;
    /**
     * 序列号
     */
    private Integer serialNum;
    /**
     * 子集合大小
     */
    private Integer size;
    /**
     * 起始坐标
     */
    private Integer start;

    /**
     * 终止坐标
     */
    public Integer getEnd() {
        return start + size;
    }
}
