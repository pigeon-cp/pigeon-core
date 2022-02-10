package com.github.taccisum.pigeon.core.entity.core.holder;

import com.github.taccisum.pigeon.core.entity.core.RawMessageDeliverer;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface MessageDelivererHolder {
    RawMessageDeliverer getMessageDeliverer();
}
