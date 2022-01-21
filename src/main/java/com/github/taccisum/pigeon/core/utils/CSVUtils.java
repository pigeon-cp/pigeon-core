package com.github.taccisum.pigeon.core.utils;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class CSVUtils {
    /**
     * 根据 header 名获取，否则按索引获取，若均不存在则返回默认值
     *
     * @param row    csv 行 record
     * @param header header name
     * @param index  索引
     * @param def    默认值
     */
    public static String getOrDefault(CSVRecord row, String header, Integer index, String def) {
        if (row == null) {
            return null;
        }

        String val = null;
        if (!StringUtils.isBlank(header)) {
            try {
                val = row.get(header);
            } catch (IllegalArgumentException ignore) {
            }
        }
        if (val == null && index != null) {
            try {
                val = row.get(index);
            } catch (ArrayIndexOutOfBoundsException ignore) {
            }
        }

        return Optional.ofNullable(val)
                .orElse(def);
    }

    public static String getOrDefault(CSVRecord row, String header, Integer index) {
        return getOrDefault(row, header, index, null);
    }

    public static String getOrDefault(CSVRecord row, String header, String def) {
        return getOrDefault(row, header, null, def);
    }

    public static String getOrDefault(CSVRecord row, String header) {
        return getOrDefault(row, header, null, null);
    }
}
