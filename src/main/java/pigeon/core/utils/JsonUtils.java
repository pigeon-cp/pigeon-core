package pigeon.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class JsonUtils {
    private static ObjectMapper objectMapper;

    public static void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    /**
     * @param json  原始 json 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 解析后的对象
     */
    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将对象转换成 json 字符串
     *
     * @param obj 目标对象
     * @return json 字符串
     */
    public static String stringify(Object obj) {
        return JsonUtils.stringify(obj, null);
    }

    /**
     * @param obj 要转换的对象
     * @param def 转换失败时的默认值
     * @return 对象转换后的 json 字符串
     */
    public static String stringify(Object obj, String def) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            if (def == null) {
                if (obj instanceof Array || obj instanceof Collection) {
                    return "[]";
                } else {
                    return "{}";
                }
            }
            return def;
        }
    }
}
