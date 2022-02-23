package pigeon.core.repo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pigeon.core.docs.DocsSource;
import pigeon.core.docs.desc.FactoryDesc;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/2/23
 */
class EntityFactoryTest {

    @Nested
    @DisplayName("#Any")
    class AnyTest {
        @Nested
        @DisplayName("#toDocs")
        class ToDocsTest {
            @Test
            @DisplayName("index")
            void index() {
                EntityFactory.MatcherSet.Any<String, String> any = new EntityFactory.MatcherSet.Any<>();
                any.add("c1");
                any.add("c2");
                any.add("c3");
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("c1 | c2 | c3");
            }

            @Test
            @DisplayName("obj")
            void obj() {
                EntityFactory.MatcherSet.Any<FooMatcher, String> any = new EntityFactory.MatcherSet.Any<>();
                any.add(new FooMatcher("c1"));
                any.add(new FooMatcher("c2"));
                any.add(new FooMatcher("c3"));
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("c1 | c2 | c3");
            }

            @Test
            @DisplayName("docsSource")
            void docsSource() {
                EntityFactory.MatcherSet.Any<FooMatcher, String> any = new EntityFactory.MatcherSet.Any<>();
                any.add(new BarMatcher("c1"));
                any.add(new BarMatcher("c2"));
                any.add(new BarMatcher("c3"));
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("val:c1 | val:c2 | val:c3");
            }

            class FooMatcher {
                String val;

                public FooMatcher(String val) {
                    this.val = val;
                }

                @Override
                public String toString() {
                    return val;
                }
            }

            class BarMatcher extends FooMatcher implements DocsSource.Factory.Matcher {
                public BarMatcher(String val) {
                    super(val);
                }

                @Override
                public FactoryDesc.MatcherDesc toDocs() {
                    return new FactoryDesc.MatcherDesc("val:" + val);
                }
            }
        }
    }
}