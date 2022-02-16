package pigeon.core.repo.factory;

import pigeon.core.entity.core.User;
import pigeon.core.repo.EntityFactory;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface UserFactory extends EntityFactory<String, User, UserFactory.Criteria> {
    @Data
    class Criteria {
    }
}
