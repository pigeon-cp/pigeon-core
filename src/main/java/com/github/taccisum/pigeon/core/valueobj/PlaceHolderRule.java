package com.github.taccisum.pigeon.core.valueobj;

/**
 * 占位符规则
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface PlaceHolderRule {
    /**
     * 解析模板内容
     *
     * @param content 模板内容
     * @param params  参数
     */
    String resolve(String content, Object params);
}
