package pigeon.core.docs.err;

import com.github.taccisum.domain.core.exception.ErrorCode;
import com.github.taccisum.domain.core.exception.ErrorCodeMapping;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/2/28
 */
class ErrorCodeFinderTest {
    @Test
    void index() {
        List<ErrorCode> all = new ErrorCodeFinder(Lists.newArrayList("pigeon.core.excp"), new ErrorCodeMapping.Default()).findAll();
        assertThat(all.size()).isEqualTo(1);
    }
}