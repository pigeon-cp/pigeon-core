package pigeon.core.dao;

import pigeon.core.data.MessageDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageDAO extends BaseDAO<MessageDO> {
    /**
     * 批量插入消息
     *
     * @param messages 消息列表
     * @since 0.2
     */
    void insertAll(List<MessageDO> messages);

    /**
     * @param massId     消息集 id
     * @param messageIds 消息 id 列表
     */
    default void updateMassIdBatch(Long massId, List<Long> messageIds) {
        this.updateMassIdBatch(massId, null, messageIds);
    }

    /**
     * 根据消息 id 批量更新 mass_id
     *
     * @param massId     消息集 id
     * @param subMassId  消息子集 id
     * @param messageIds 消息 id 列表
     */
    default void updateMassIdBatch(Long massId, Long subMassId, List<Long> messageIds) {
        MessageDO o = newEmptyDataObject();
        o.setMassId(massId);
        o.setSubMassId(subMassId);
        this.updateBatchByIdList(o, messageIds);
    }


    /**
     * 根据消息 id 批量更新数据
     *
     * @param o          更新对象
     * @param messageIds 消息 id 列表
     * @since 0.2
     */
    void updateBatchByIdList(MessageDO o, List<Long> messageIds);

    /**
     * @param massId mass id
     * @param limit  最大限制数量
     * @return 根据 mass id 查找到的所有消息
     */
    List<? extends MessageDO> selectListByMassId(Long massId, long limit);

    /**
     * @param subMassId sub mass id
     * @return 根据 sub mass id 查找到的所有消息
     */
    List<? extends MessageDO> selectListBySubMassId(Long subMassId);
}
