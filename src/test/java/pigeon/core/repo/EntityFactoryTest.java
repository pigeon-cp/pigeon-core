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
                EntityFactory.CriteriaSet.Any<String> any = new EntityFactory.CriteriaSet.Any<>();
                any.add("c1");
                any.add("c2");
                any.add("c3");
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("c1 | c2 | c3");
            }

            @Test
            @DisplayName("obj")
            void obj() {
                EntityFactory.CriteriaSet.Any<FooCriteria> any = new EntityFactory.CriteriaSet.Any<>();
                any.add(new FooCriteria("c1"));
                any.add(new FooCriteria("c2"));
                any.add(new FooCriteria("c3"));
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("c1 | c2 | c3");
            }

            @Test
            @DisplayName("docsSource")
            void docsSource() {
                EntityFactory.CriteriaSet.Any<FooCriteria> any = new EntityFactory.CriteriaSet.Any<>();
                any.add(new BarCriteria("c1"));
                any.add(new BarCriteria("c2"));
                any.add(new BarCriteria("c3"));
                FactoryDesc desc = any.toDocs();
                assertThat(desc.value()).isEqualTo("val:c1 | val:c2 | val:c3");
            }

            class FooCriteria {
                String val;

                public FooCriteria(String val) {
                    this.val = val;
                }

                @Override
                public String toString() {
                    return val;
                }
            }

            class BarCriteria extends FooCriteria implements DocsSource.Factory.Criteria {
                public BarCriteria(String val) {
                    super(val);
                }

                @Override
                public FactoryDesc.CriteriaDesc toDocs() {
                    return new FactoryDesc.CriteriaDesc("val:" + val);
                }
            }
        }
    }
}