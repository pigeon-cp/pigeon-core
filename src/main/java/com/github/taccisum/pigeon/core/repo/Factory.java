package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.entity.core.*;
import com.github.taccisum.pigeon.core.repo.factory.*;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class Factory implements com.github.taccisum.domain.core.Factory {
    private final PluginManager pluginManager;

    @Autowired
    public Factory(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public MessageTemplate createMessageTemplate(long id, String type, String spType) {
        return this.create(
                id,
                new MessageTemplateFactory.Criteria(type, spType),
                MessageTemplateFactory.class
        );
    }

    public Message createMessage(long id, String type, String spType) {
        return this.create(
                id,
                new MessageFactory.Criteria(type, spType),
                MessageFactory.class
        );
    }

    public ServiceProvider createServiceProvider(String id) {
        return this.create(
                id,
                new ServiceProviderFactory.Criteria(id),
                ServiceProviderFactory.class
        );
    }

    public ThirdAccount createThirdAccount(long id, String username, String spType) {
        return this.create(
                id,
                new ThirdAccountFactory.Criteria(username, spType),
                ThirdAccountFactory.class
        );
    }

    public User createUser(String id) {
        return this.create(id, new UserFactory.Criteria(), UserFactory.class);
    }

    public MassTactic createMassTactic(Long id, String type) {
        return this.create(id, new MassTacticFactory.Criteria(type), MassTacticFactory.class);
    }

    public MessageMass createMessageMass(Long id) {
        return new MessageMass(id);
    }

    /**
     * 创建指定实体实例
     *
     * @param id       实体 id
     * @param criteria 匹配条件
     * @param type     实体工厂类型
     */
    <ID extends Serializable, E extends Entity<ID>, C, F extends EntityFactory<ID, E, C>>
    E create(ID id, C criteria, Class<F> type) {
        List<F> factories = pluginManager.getExtensions(type);
        // TODO:: should cache ordered result for perf optimization.
        factories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (F factory : factories) {
            if (factory.match(id, criteria)) {
                return factory.create(id, criteria);
            }
        }

        throw new UnsupportedOperationException(String.format("Not any factory matched(for id %s, criteria: %s. expected factory type: %s).", id, criteria, type.getSimpleName()));
    }

    public <ID extends Serializable, E extends CustomConcept<ID>, C, F extends CustomConceptFactory<ID, E, C>> E createExt(ID id, C criteria, Class<F> type) {
        List<F> factories = pluginManager.getExtensions(type);
        // TODO:: should cache ordered result for perf optimization.
        factories.sort(Comparator.comparingInt(CustomConceptFactory::getOrder));
        for (F factory : factories) {
            if (factory.match(id, criteria)) {
                return factory.create(id, criteria);
            }
        }

        throw new UnsupportedOperationException(String.format("Not any extend factory matched(for id %s, criteria: %s. expected factory type: %s).", id, criteria, type.getSimpleName()));
    }
}
