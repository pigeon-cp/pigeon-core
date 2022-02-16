package pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import lombok.Getter;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/17
 */
@Import(Factory.class)
@ExtendWith(SpringExtension.class)
class FactoryTest {
    @Autowired
    private Factory factory;
    @MockBean
    private PluginManager pluginManager;

    @Test
    void index() {
        assertThat(factory).isNotNull();
        assertThat(pluginManager).isNotNull();
    }

    @Nested
    @DisplayName("#create(...)")
    class CreateTest {
        @Test
        @DisplayName("index")
        void index() {
            List<FooFactory> factories = new ArrayList<>();
            factories.add(new FooFactory(1));

            when(pluginManager.getExtensions(FooFactory.class)).thenReturn(factories);
            Foo foo = factory.create(1001L, null, FooFactory.class);
            assertThat(foo).isNotNull();
            assertThat(foo.getId()).isEqualTo(1001L);
            assertThat(foo.getMark()).isEqualTo(1);
        }

        @Test
        @DisplayName("匹配 factory 前应该先按 #getOrder() 升序排序")
        void shouldOrderBeforeMatch() {
            List<FooFactory> factories = new ArrayList<>();
            factories.add(new FooFactory(99));
            factories.add(new FooFactory(1) {
                @Override
                public boolean match(Long id, Object criteria) {
                    return false;
                }
            });
            factories.add(new FooFactory(10));
            factories.add(new FooFactory(50));

            when(pluginManager.getExtensions(FooFactory.class)).thenReturn(factories);
            Foo foo = factory.create(1001L, null, FooFactory.class);
            assertThat(foo).isNotNull();
            // 先排序再 match，由于 1 match 时 return false，因此应该是 10 最终匹配到
            assertThat(foo.getMark()).isEqualTo(10);
        }

        @Test
        @DisplayName("若无任何 factory 匹配时应抛出异常")
        void throwIfNotFactoryFoundOrMatched() {
            List<FooFactory> factories = new ArrayList<>();
            when(pluginManager.getExtensions(FooFactory.class)).thenReturn(factories);

            Assert.assertThrows(UnsupportedOperationException.class, () -> {
                factory.create(1001L, null, FooFactory.class);
            });

            factories.add(new FooFactory(1) {
                @Override
                public boolean match(Long id, Object criteria) {
                    return false;
                }
            });
            Assert.assertThrows(UnsupportedOperationException.class, () -> {
                factory.create(1001L, null, FooFactory.class);
            });
        }
    }

    static class Foo extends Entity.Base<Long> {
        @Getter
        private int mark;

        public Foo(Long id, int mark) {
            super(id);
            this.mark = mark;
        }
    }

    static class FooFactory implements EntityFactory<Long, Foo, Object> {
        private int order;

        public FooFactory(int order) {
            this.order = order;
        }

        @Override
        public Foo create(Long id, Object criteria) {
            return new Foo(id, order);
        }

        @Override
        public boolean match(Long id, Object criteria) {
            return true;
        }

        @Override
        public int getOrder() {
            return this.order;
        }
    }
}