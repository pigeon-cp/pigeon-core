package pigeon.core.dao;

import java.io.Serializable;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface BaseDAO<T> {
    /**
     * 新增一条数据
     *
     * @param data 数据
     * @return 唯一 id
     */
    <ID extends Serializable> ID insert(T data);

    /**
     * 根据 id 查询数据
     */
    T selectById(Serializable id);

    /**
     * 根据 id 更新数据
     */
    void updateById(T data);

    /**
     * 创建一个空的 DO  实例
     */
    T newEmptyDataObject();
}
