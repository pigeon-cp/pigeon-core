package pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import org.pf4j.ExtensionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import pigeon.core.docs.DocsSource;
import pigeon.core.docs.desc.FactoryDesc;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <pre>
 * 扩展点：实体工厂
 *
 * 实现此扩展点以扩展实体创建过程，你可以通过 {@link #create} 方法创建属于你插件自己的实体实现，并在其中实现特殊逻辑，从而达到扩展领域模型的能力的效果。
 *
 * 通过实体工厂创建出来的实体可以通过 {@link Autowired} 或 {@link Resource} 注解来注入主程序中定义的 bean，使用这些 bean 提供的能力可以帮助你更好地完成插件功能
 * </pre>
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface EntityFactory<ID extends Serializable, E extends Entity<ID>, C> extends Ordered, ExtensionPoint {
    /**
     * 创建实体
     *
     * @param id       实体 id
     * @param criteria 参数
     */
    E create(ID id, C criteria);

    /**
     * 匹配条件
     *
     * @param id       实体 id
     * @param criteria 参数
     */
    boolean match(ID id, C criteria);

    /**
     * 优先级，数字越小优先级越高
     */
    @Override
    default int getOrder() {
        // 默认 0
        return 0;
    }

    abstract class Base<ID extends Serializable, E extends Entity<ID>, C> implements
            EntityFactory<ID, E, C>,
            DocsSource.Factory {
        @Override
        public boolean match(ID id, C criteria) {
            return this.getCriteriaSet().match(criteria);
        }

        /**
         * 匹配的条件集
         */
        public CriteriaSet<C> getCriteriaSet() {
            return new CriteriaSet.None<>();
        }

        @Override
        public FactoryDesc toDocs() {
            return this.getCriteriaSet().toDocs();
        }
    }

    /**
     * factory 匹配条件集
     */
    interface CriteriaSet<C> extends DocsSource<FactoryDesc> {
        Set<C> get();

        /**
         * 是否能匹配上给定条件
         */
        boolean match(C c);

        class None<C> implements CriteriaSet<C> {
            @Override
            public Set<C> get() {
                return Collections.EMPTY_SET;
            }

            @Override
            public boolean match(C c) {
                // always false
                return false;
            }

            @Override
            public FactoryDesc toDocs() {
                return null;
            }
        }

        /**
         * TODO: not implement
         *
         * @param <C>
         */
        class Any<C> implements CriteriaSet<C> {
            private Set<C> set = new LinkedHashSet<>();

            public Any<C> add(C criteria) {
                set.add(criteria);
                return this;
            }

            @Override
            public Set<C> get() {
                return set;
            }

            @Override
            public boolean match(C c) {
                return this.set.contains(c);
            }

            @Override
            public FactoryDesc toDocs() {
                if (CollectionUtils.isEmpty(this.set)) {
                    return null;
                }
                String val = this.set.stream()
                        .map(c -> {
                            if (c instanceof DocsSource.Factory.Criteria) {
                                FactoryDesc.CriteriaDesc docs = ((Factory.Criteria) c).toDocs();
                                return String.format("%s(%s)", docs.value(), docs.desc());
                            } else {
                                return c.toString();
                            }
                        })
                        .reduce((p, c) -> {
                            return String.format("%s | %s", p, c);
                        })
                        .get();
                return new FactoryDesc(val);
            }
        }

        class All<C> implements CriteriaSet<C> {
            @Override
            public Set<C> get() {
                return null;
            }

            @Override
            public boolean match(C c) {
                // always true
                return true;
            }

            @Override
            public FactoryDesc toDocs() {
                return new FactoryDesc("*");
            }
        }
    }

}
