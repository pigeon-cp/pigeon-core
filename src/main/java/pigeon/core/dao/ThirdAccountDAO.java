package pigeon.core.dao;

import pigeon.core.data.ThirdAccountDO;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ThirdAccountDAO extends BaseDAO<ThirdAccountDO> {
    /**
     * @param name 账号名称
     * @return 根据账号名称获取到的三方账号
     */
    List<? extends ThirdAccountDO> selectByUsername(String name);
}
