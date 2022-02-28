package pigeon.core.dao;

import pigeon.core.data.SubMassDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface SubMassDAO extends BaseDAO<SubMassDO> {
    /**
     * @param mainId main mass id
     * @return 根据所属消息集 id 查找到的所有子集
     */
    List<SubMassDO> selectByMainId(Long mainId);
}
