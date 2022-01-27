package com.github.taccisum.pigeon.core.dao;

import com.github.taccisum.pigeon.core.data.SubMassDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface SubMassDAO extends BaseDAO<SubMassDO> {
    /**
     * 根据所属消息集 id 查找所有子集
     *
     * @param mainId main mass id
     */
    List<SubMassDO> selectByMainId(Long mainId);
}
