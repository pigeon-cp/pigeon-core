package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.sp.DefaultServiceProvider;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;
import org.apache.commons.lang.NotImplementedException;
import org.pf4j.Extension;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ServiceProviderFactory extends EntityFactory<String, ServiceProvider, ServiceProviderFactory.Criteria> {
    @Data
    class Criteria {
        String spType;

        public Criteria(String spType) {
            this.spType = spType;
        }
    }

    @Extension
    class Default implements ServiceProviderFactory {
        @Override
        public ServiceProvider create(String id, Criteria criteria) {
            switch (criteria.getSpType()) {
                case "PIGEON":
                    throw new NotImplementedException();
                default:
                    return new DefaultServiceProvider(id);
            }
        }

        @Override
        public boolean match(String s, Criteria criteria) {
            return true;
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }
    }
}
