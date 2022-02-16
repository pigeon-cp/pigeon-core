package pigeon.core.repo;

import com.github.taccisum.domain.core.exception.DataNotFoundException;
import pigeon.core.dao.UserDAO;
import pigeon.core.data.UserDO;
import pigeon.core.entity.core.User;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Slf4j
@Component
public class UserRepo {
    //    private static final String PLUGIN_ID = "pigeon-user";
    private static final String PLUGIN_ID = null;

    @Resource
    private PluginManager pluginManager;
    @Resource
    private Factory factory;

    /**
     * 根据 id 获取用户
     *
     * @param id 用户 id
     */
    public Optional<User> get(String id) {
        UserDAO dao = this.getDao();
        UserDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }

        return Optional.of(factory.createUser(id));
    }

    /**
     * 根据 id 获取用户（失败时抛出异常）
     *
     * @param id 用户 id
     * @throws UserNotFoundException 用户不存在
     */
    public User getOrThrow(String id) throws UserNotFoundException {
        return this.get(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    static final Class<UserDAO> EXTENSION_CLAZZ = UserDAO.class;
    private UserDAO getDao() {
        // TODO:: plugin id 应可配置
        List<UserDAO> daoLs = null;
        if (PLUGIN_ID == null) {
            daoLs = pluginManager.getExtensions(EXTENSION_CLAZZ);
        } else {
            daoLs = pluginManager.getExtensions(EXTENSION_CLAZZ, PLUGIN_ID);
        }
        if (daoLs == null || daoLs.size() == 0) {
            log.warn("{} 未找到任何扩展点，相关功能可能会受影响.", EXTENSION_CLAZZ.getSimpleName());
            return new UserDAO.Dummy();
        }
        if (daoLs.size() > 1) {
            log.warn("{} 暂不支持多个扩展点，将只有第一个生效.", EXTENSION_CLAZZ.getSimpleName());
        }
        return daoLs.get(0);
    }

    public static class UserNotFoundException extends DataNotFoundException {
        public UserNotFoundException(String id) {
            super("用户", id);
        }
    }
}
