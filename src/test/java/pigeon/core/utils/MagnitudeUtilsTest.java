package pigeon.core.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/2/9
 */
class MagnitudeUtilsTest {

    @Test
    void index() {
        assertThat(MagnitudeUtils.fromInt(-1)).isEqualTo(MagnitudeUtils.Level.LESS);
        assertThat(MagnitudeUtils.fromInt(0)).isEqualTo(MagnitudeUtils.Level.LESS);
        assertThat(MagnitudeUtils.fromInt(1)).isEqualTo(MagnitudeUtils.Level.LESS);
        assertThat(MagnitudeUtils.fromInt(9)).isEqualTo(MagnitudeUtils.Level.LESS);
        assertThat(MagnitudeUtils.fromInt(90)).isEqualTo(MagnitudeUtils.Level.LESS);
        assertThat(MagnitudeUtils.fromInt(100)).isEqualTo(MagnitudeUtils.Level.HUNDREDS);
        assertThat(MagnitudeUtils.fromInt(999)).isEqualTo(MagnitudeUtils.Level.HUNDREDS);
        assertThat(MagnitudeUtils.fromInt(1000)).isEqualTo(MagnitudeUtils.Level.THOUSANDS);
        assertThat(MagnitudeUtils.fromInt(9999)).isEqualTo(MagnitudeUtils.Level.THOUSANDS);
    }
}