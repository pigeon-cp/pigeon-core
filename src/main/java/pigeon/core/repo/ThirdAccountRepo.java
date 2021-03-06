package pigeon.core.repo;

import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.domain.core.exception.DataNotFoundException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import org.springframework.stereotype.Component;
import pigeon.core.dao.ThirdAccountDAO;
import pigeon.core.data.ThirdAccountDO;
import pigeon.core.entity.core.ThirdAccount;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class ThirdAccountRepo {
    @Resource
    private ThirdAccountDAO dao;
    @Resource
    private Factory factory;

    public ThirdAccount create(ThirdAccountDO data) {
        dao.insert(data);
        return factory.createThirdAccount(data.getId(), data.getUsername(), data.getType(), data.getSpType());
    }

    public Optional<ThirdAccount> getByUsername(String name) {
        List<? extends ThirdAccountDO> ls = dao.selectByUsername(name);
        if (ls.size() > 1) {
            throw new DataErrorException("三方账号", null, String.format("账号 %s 名存在多条数据", name));
        }
        if (ls.size() == 0) {
            return Optional.empty();
        }
        ThirdAccountDO data = ls.get(0);
        return Optional.of(factory.createThirdAccount(data.getId(), data.getUsername(), data.getType(), data.getSpType()));
    }

    public Optional<ThirdAccount> get(long id) {
        ThirdAccountDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createThirdAccount(data.getId(), data.getUsername(), data.getType(), data.getSpType()));
    }

    @ErrorCode(value = "THIRD_ACCOUNT", inherited = true, description = "三方账号不存在")
    public static class NotFoundException extends DataNotFoundException {
        public NotFoundException(long id, String sp) {
            super(String.format("三方账号 %d 不存在或不属于 %s", id, sp));
        }

        public NotFoundException(long id) {
            super("三方账号", id);
        }
    }
}
