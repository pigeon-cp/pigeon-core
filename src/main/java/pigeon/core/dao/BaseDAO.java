package pigeon.core.dao;

import pigeon.core.data.DataObject;

import java.io.Serializable;

/**
 * @param <T> 此 DAO 管理的数据对象类型
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface BaseDAO<T extends DataObject> {
    /**
     * 新增一条数据
     *
     * @param data 数据
     * @param <ID> id 类型
     * @return 唯一 id
     */
    <ID extends Serializable> ID insert(T data);

    /**
     * @param id 目标 id
     * @return 根据 id 查询的数据
     */
    T selectById(Serializable id);

    /**
     * 根据 id 更新数据
     *
     * @param data 数据对象
     */
    void updateById(T data);

    /**
     * @return 新建的空的 DO 实例
     */
    T newEmptyDataObject();
}
