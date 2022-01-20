package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import org.apache.commons.lang.NotImplementedException;

/**
 * 消息子集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public class SubMass extends Entity.Base<Long> {
    public SubMass(Long id) {
        super(id);
    }

    /**
     * 投递此消息子集
     */
    public void deliver() {
        throw new NotImplementedException();
    }
}
