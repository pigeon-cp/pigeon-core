package pigeon.core.valueobj;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/28
 */
class SourceTest {
    @Test
    @DisplayName("index")
    void index() {
        String text = "target\n1\n2\n3";
        Source.Text source = new Source.Text(text);
        assertThat(source.getSource()).isEqualTo(text);
        assertThat(source.size()).isEqualTo(3);
    }
}