package com.github.taccisum.pigeon.core.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class TolerantCastUtils {
    /**
     * Object 转 Array，支持传入数组、集合、Map、JSON 字符串，普通字符串（英文逗号分割），转换失败时将返回空数组
     */
    public static Object[] toArray(Object obj) {
        Object[] vars = null;
        if (obj == null) {
            vars = new Object[]{};
        } else if (obj.getClass().isArray()) {
            vars = (Object[]) obj;
        } else if (obj instanceof Collection) {
            Collection ls = (Collection) obj;
            vars = ls.toArray();
        } else if (obj instanceof Map) {
            vars = ((Map<?, ?>) obj).values().toArray();
        } else if (obj instanceof String) {
            String str = (String) obj;
            if (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']') {
                vars = JsonUtils.parse(str, String[].class);
            } else {
                vars = str.split(",");
            }
        } else {
            vars = new Object[]{};
        }
        return vars;
    }
}
