package com.github.taccisum.pigeon.core.repo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.domain.core.exception.DataNotFoundException;
import com.github.taccisum.pigeon.core.dao.ThirdAccountDAO;
import com.github.taccisum.pigeon.core.data.ThirdAccountDO;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import org.springframework.stereotype.Component;

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

    public Optional<ThirdAccount> getByUsername(String name) {
        List<ThirdAccountDO> ls = dao.selectByUsername(name);
        if (ls.size() > 1) {
            throw new DataErrorException("三方账号", null, String.format("账号 %s 名存在多条数据", name));
        }
        if (ls.size() == 0) {
            return Optional.empty();
        }
        ThirdAccountDO data = ls.get(0);
        return Optional.of(factory.createThirdAccount(data.getId(), data.getUsername(), data.getSpType()));
    }

    public Optional<ThirdAccount> get(long id) {
        ThirdAccountDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createThirdAccount(data.getId(), data.getUsername(), data.getSpType()));
    }

    public static class NotFoundException extends DataNotFoundException {
        public NotFoundException(long id, String sp) {
            super(String.format("三方账号 %d 不存在或不属于 %s", id, sp));
        }

        public NotFoundException(long id) {
            super("三方账号", id);
        }
    }
}
