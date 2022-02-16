package pigeon.core.dao;

import pigeon.core.data.UserDO;
import org.pf4j.ExtensionPoint;

/**
 * 扩展点：用户数据访问对象
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface UserDAO extends ExtensionPoint {
    UserDO selectById(String id);

    class Dummy implements UserDAO {
        @Override
        public UserDO selectById(String id) {
            return null;
        }
    }
}
