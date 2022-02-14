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
    /**
     * 消息发送人
     */
    private String sender;
    /**
     * 接收人账号
     */
    private Object account;
    /**
     * 模板参数
     */
    private String params;
    /**
     * 消息签名
     */
    private String signature;
    /**
     * 自定义拓展参数
     */
    private String ext;
}
