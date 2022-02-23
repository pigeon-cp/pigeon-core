package pigeon.core.docs.desc;

import lombok.Data;
import lombok.experimental.Accessors;
import pigeon.core.docs.Description;

/**
 * 实体工厂描述
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Data
@Accessors(chain = true, fluent = true)
public
class FactoryDesc implements Description {
    private String value;

    public FactoryDesc(String value) {
        this.value = value;
    }

    /**
     * 匹配条件描述
     */
    @Data
    @Accessors(chain = true, fluent = true)
    public static class MatcherDesc implements Description {
        private String value;
        private String desc;

        public MatcherDesc(String value) {
            this.value = value;
        }
    }
}
