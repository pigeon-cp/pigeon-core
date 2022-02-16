package pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import pigeon.core.dao.ThirdAccountDAO;
import pigeon.core.data.ThirdAccountDO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 三方账号
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class ThirdAccount extends Entity.Base<Long> {
    @Autowired
    private ThirdAccountDAO dao;

    public ThirdAccount(long id) {
        super(id);
    }

    public ThirdAccountDO data() {
        return dao.selectById(this.id());
    }
}
