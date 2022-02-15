package com.github.taccisum.pigeon.core.utils;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class InfoUtils {
    /**
     * <pre>
     * 省略超出长度的信息，并替换最后三个字符为 "..."
     *
     * 例：
     * - omit('abcdefg', 5) -> ab...
     * - omit('abcd', 5) -> abcd
     * </pre>
     */
    public static String omit(String info, int size) {
        if (info == null) {
            return null;
        }
        if (info.length() > size) {
            if (size > 3) {
                return info.substring(0, size - 3) + "...";
            } else {
                return info.substring(0, size);
            }
        } else {
            return info;
        }
    }
}
