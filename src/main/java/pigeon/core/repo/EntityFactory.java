package pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import org.pf4j.ExtensionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.io.Serializable;

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
     * @param id       实体 id
     * @param criteria 参数
     * @return 新建的实体
     */
    E create(ID id, C criteria);

    /**
     * 条件是否匹配当前 factory
     *
     * @param id       实体 id
     * @param criteria 参数
     * @return true: 匹配，false: 不匹配
     */
    boolean match(ID id, C criteria);

    /**
     * @return factory 作用优先级，数字越小优先级越高
     */
    @Override
    default int getOrder() {
        // 默认 0
        return 0;
    }
}
