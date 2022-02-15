package com.github.taccisum.pigeon.core.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/2/15
 */
class InfoUtilsTest {

    @Test
    @DisplayName("index")
    void index() {
        assertThat(InfoUtils.omit("abcdefg", 5)).isEqualTo("ab...");
        assertThat(InfoUtils.omit("abcd", 5)).isEqualTo("abcd");
    }

    @Test
    @DisplayName("允许 Null 值")
    void handleNull() {
        assertThat(InfoUtils.omit(null, 0)).isEqualTo(null);
    }
}