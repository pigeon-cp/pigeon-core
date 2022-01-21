package com.github.taccisum.pigeon.core.valueobj;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 群发策略的消息配置
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
@Accessors(chain = true)
public class MessageInfo {
    private String sender;
    private Object account;
    private String params;
}
