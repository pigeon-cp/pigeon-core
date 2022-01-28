package com.github.taccisum.pigeon.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/28
 */
class TolerantCastUtilsTest {
    @BeforeEach
    void setUp() {
        JsonUtils.setObjectMapper(new ObjectMapper());
    }

    @ParameterizedTest
    @DisplayName("传入数组、集合、Map、JSON 数组、逗号分割字符串均能解析")
    @MethodSource("indexSource")
    void index(Object obj) {
        Object[] arr = TolerantCastUtils.toArray(obj);
        assertThat(arr.length).isEqualTo(3);
        assertThat(arr).containsOnly("1", "2", "3");
    }

    static Object indexSource() {
        Map<Integer, String> map = Maps.newHashMap(0, "1");
        map.put(1, "2");
        map.put(2, "3");
        return new Object[]{
                wrap(new String[]{"1", "2", "3"}),
                Sets.newHashSet("1", "2", "3"),
                Lists.newArrayList("1", "2", "3"),
                map,
                "[\"1\",\"2\",\"3\"]",
                "[1,2,3]",
                "1,2,3"
        };
    }

    @ParameterizedTest
    @DisplayName("解析失败时返回空数组")
    @MethodSource("failSource")
    void fail(Object obj) {
        Object[] arr = TolerantCastUtils.toArray(obj);
        assertThat(arr).isNotNull();
        assertThat(arr.length).isEqualTo(0);
    }

    static Object failSource() {
        return new Object[]{
                null,
                new Long(1L),
                new TolerantCastUtilsTest(),
                new HashMap<>()
        };
    }

    private static Object[] wrap(Object obj) {
        return new Object[]{obj};
    }
}