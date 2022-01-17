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
        List<MessageTemplateFactory> messageTemplateFactories = pluginManager.getExtensions(MessageTemplateFactory.class);
        messageTemplateFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (MessageTemplateFactory factory : messageTemplateFactories) {
            MessageTemplateFactory.Criteria criteria = new MessageTemplateFactory.Criteria(type, spType);
            if (factory.match(id, criteria)) {
                return factory.create(id, criteria);
            }
        }
        throw new UnsupportedOperationException("not any message template factory matched.");
    }

    public Message createMessage(long id, String type, String spType) {
        List<MessageFactory> messageFactories = pluginManager.getExtensions(MessageFactory.class);
        messageFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (MessageFactory factory : messageFactories) {
            MessageFactory.Criteria criteria = new MessageFactory.Criteria(type, spType);
            if (factory.match(id, criteria)) {
                return factory.create(id, criteria);
            }
        }
        throw new UnsupportedOperationException("not any message factory matched.");
    }

    @Deprecated
    public ServiceProvider createServiceProvider(ServiceProvider.Type id) {
        return createServiceProvider(id.name());
    }

    public ServiceProvider createServiceProvider(String id) {
        List<ServiceProviderFactory> serviceProviderFactories = pluginManager.getExtensions(ServiceProviderFactory.class);
        serviceProviderFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (ServiceProviderFactory factory : serviceProviderFactories) {
            if (factory.match(id, new ServiceProviderFactory.Criteria(id))) {
                return factory.create(id, null);
            }
        }
        throw new UnsupportedOperationException("not any service provide factory matched.");
    }

    @Deprecated
    public ThirdAccount createThirdAccount(long id, ServiceProvider.Type spType) {
        return createThirdAccount(id, spType.name());
    }

    public ThirdAccount createThirdAccount(long id, String spType) {
        List<ThirdAccountFactory> thirdAccountFactories = pluginManager.getExtensions(ThirdAccountFactory.class);
        thirdAccountFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (ThirdAccountFactory factory : thirdAccountFactories) {
            ThirdAccountFactory.Criteria criteria = new ThirdAccountFactory.Criteria(spType);
            if (factory.match(id, criteria)) {
                return factory.create(id, criteria);
            }
        }
        throw new UnsupportedOperationException("not any third account factory matched.");
    }

    public User createUser(String id) {
        return this.create(id, new UserFactory.Criteria(), UserFactory.class);
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
}
