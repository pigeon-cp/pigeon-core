package com.github.taccisum.pigeon.core.service;

/**
 * 异步指令服务
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface AsyncCommandService<T> {
    /**
     * 发布指令
     */
    void publish(T command);

    /**
     * 处理指令
     */
    void handle(T command);
}
