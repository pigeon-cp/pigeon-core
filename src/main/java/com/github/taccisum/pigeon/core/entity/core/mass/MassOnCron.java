package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.MassTactic;

/**
 * 时间满足 cron 表达式时发送
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MassOnCron extends MassTactic {
    public MassOnCron(Long id) {
        super(id);
    }
}
