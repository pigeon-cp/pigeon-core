package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import org.springframework.core.Ordered;

import java.io.Serializable;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface EntityFactory<ID extends Serializable, E extends Entity<ID>, O> extends Ordered {
    E create(ID id);

    boolean match(ID id, O o);

    @Override
    default int getOrder() {
        return Integer.MIN_VALUE;
    }
}
