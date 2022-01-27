package com.github.taccisum.pigeon.core.dao;

import com.github.taccisum.pigeon.core.data.MessageDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageDAO extends BaseDAO<MessageDO> {
    default void updateMassIdBatch(Long massId, List<Long> messageIds) {
        this.updateMassIdBatch(massId, null, messageIds);
    }

    /**
     * 根据消息 id 批量更新 mass_id
     *
     * @param massId     mass id
     * @param subMassId  sub mass id
     * @param messageIds 消息 id
     */
    void updateMassIdBatch(Long massId, Long subMassId, List<Long> messageIds);

    /**
     * 根据 mass id 查找所有消息
     *
     * @param massId mass id
     * @param limit  最大限制数量
     */
    List<MessageDO> selectListByMassId(Long massId, long limit);

    /**
     * 根据 sub mass id 查找所有消息
     *
     * @param subMassId sub mass id
     */
    List<MessageDO> selectListBySubMassId(Long subMassId);
}
