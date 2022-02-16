package pigeon.core.entity.core.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/2/15
 */
class MailTest {
    @Test
    @DisplayName("index")
    void index() {
        String[][] targets = Mail.resolveTargets("to:001@qq.com;cc:002@qq.com,003@qq.com;bcc:004@gmail.com,005@gmail.com");
        assertThat(targets[0]).containsOnly("001@qq.com");
        assertThat(targets[1]).containsOnly("002@qq.com", "003@qq.com");
        assertThat(targets[2]).containsOnly("004@gmail.com", "005@gmail.com");
    }

    @Test
    @DisplayName("target 空不报错")
    void handleEmpty() {
        String[][] targets = Mail.resolveTargets("");
        assertThat(targets[0][0]).isNull();
        assertThat(targets[1]).isNull();
        assertThat(targets[2]).isNull();
    }

    @Test
    @DisplayName("key 空时解析为 to")
    void emptyKeyAsTo() {
        String[][] targets = Mail.resolveTargets("001@qq.com");
        assertThat(targets[0]).containsOnly("001@qq.com");
    }
}