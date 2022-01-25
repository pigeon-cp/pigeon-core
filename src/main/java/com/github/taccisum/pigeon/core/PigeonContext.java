package com.github.taccisum.pigeon.core;

import org.springframework.context.ApplicationContext;

/**
 * Pigeon 上下文
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class PigeonContext {
    /**
     * 主程序上下文
     */
    private static ApplicationContext mainContext;

    public static void setMainContext(ApplicationContext mainContext) {
        PigeonContext.mainContext = mainContext;
    }

    public static <T> T getRepo(Class<T> clazz) {
        return mainContext.getBean(clazz);
    }
}
