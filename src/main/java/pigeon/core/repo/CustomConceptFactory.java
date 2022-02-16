package pigeon.core.repo;

import pigeon.core.entity.core.CustomConcept;

import java.io.Serializable;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface CustomConceptFactory<ID extends Serializable, E extends CustomConcept<ID>, C> extends EntityFactory<ID, E, C> {
    /**
     * 创建自定义概念实体
     *
     * @param id       实体 id
     * @param criteria 参数
     */
    @Override
    E create(ID id, C criteria);

    /**
     * 匹配条件
     *
     * @param id       实体 id
     * @param criteria 参数
     */
    @Override
    boolean match(ID id, C criteria);
}
