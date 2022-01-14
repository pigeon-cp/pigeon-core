package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/12
 */
public interface ServiceProviderFactory extends EntityFactory<String, ServiceProvider, ServiceProviderFactory.Criteria> {
    @Data
    class Criteria {
        String spType;

        public Criteria(String spType) {
            this.spType = spType;
        }
    }
}
