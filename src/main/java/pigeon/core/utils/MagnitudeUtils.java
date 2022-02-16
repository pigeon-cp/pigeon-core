package pigeon.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class MagnitudeUtils {
    /**
     * 获取整形数字的数量级
     *
     * @param num 数值
     * @return 数值的数量级
     */
    public static Level fromInt(int num) {
        int zeros = 0;
        int origin = num;
        if (num >= 10) {
            do {
                num = num / 10;
                zeros++;
            } while (num >= 10);
        }
        if (zeros < 2) {
            return Level.LESS;
        }
        if (zeros >= 6) {
            return Level.MILLIONS;
        }
        Level level = Level.quickMap.get(zeros);
        if (level != null) {
            return level;
        }
        throw new IllegalArgumentException(Integer.toString(origin));
    }

    public enum Level {
        /**
         * 低于一百
         */
        LESS,
        /**
         * 百级
         */
        HUNDREDS,
        /**
         * 千级
         */
        THOUSANDS,
        /**
         * 万级
         */
        TEN_THOUSANDS,
        /**
         * 十万级
         */
        HUNDRED_THOUSANDS,
        /**
         * 百万级
         */
        MILLIONS;

        static Map<Integer, Level> quickMap = new HashMap<>();

        static {
            quickMap.put(2, HUNDREDS);
            quickMap.put(3, THOUSANDS);
            quickMap.put(4, TEN_THOUSANDS);
            quickMap.put(5, HUNDRED_THOUSANDS);
            quickMap.put(6, MILLIONS);
        }
    }
}
