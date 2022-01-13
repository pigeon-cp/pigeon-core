package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;

/**
 * 服务提供商
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ServiceProvider extends Entity<String> {
    default String getType() {
        return this.id();
    }

    @Deprecated
    enum Type {
        /**
         * 阿里云
         */
        ALI_CLOUD,
    }
}
