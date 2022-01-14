package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class ServiceProviderRepo {
    private static Map<String, ServiceProvider> spMap = new HashMap<>();

    @Autowired
    private Factory factory;

    public ServiceProvider get(String id) {
        ServiceProvider sp = spMap.get(id);
        if (sp == null) {
            sp = factory.createServiceProvider(id);
            spMap.put(id, sp);
        }

        if (sp == null) {
            throw new IllegalArgumentException(id);
        }
        return sp;
    }
}
