package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ThirdAccountFactory extends EntityFactory<Long, ThirdAccount, ThirdAccountFactory.Criteria> {
    @Data
    class Criteria {
        String spType;

        public Criteria(String spType) {
            this.spType = spType;
        }
    }
}
