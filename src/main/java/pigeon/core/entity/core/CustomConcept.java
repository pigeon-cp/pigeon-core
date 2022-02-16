package pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;

/**
 * 代表实现此接口的实体为自定义概念，在你的插件希望扩展领域模型时可以让实体实现此接口
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface CustomConcept<T> extends Entity<T> {
    abstract class Base<T> extends Entity.Base<T> implements CustomConcept<T> {
        public Base(T id) {
            super(id);
        }
    }
}
