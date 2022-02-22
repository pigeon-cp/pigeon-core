package pigeon.core.dao;

import pigeon.core.data.ThirdAccountDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ThirdAccountDAO extends BaseDAO<ThirdAccountDO> {
    /**
     * 根据账号名称获取三方账号
     *
     * @param name 账号名称
     */
    List<? extends ThirdAccountDO> selectByUsername(String name);
}
