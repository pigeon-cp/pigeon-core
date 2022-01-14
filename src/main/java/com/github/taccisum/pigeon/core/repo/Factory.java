package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import com.github.taccisum.pigeon.core.entity.core.ThirdAccount;
import com.github.taccisum.pigeon.core.repo.factory.MessageFactory;
import com.github.taccisum.pigeon.core.repo.factory.MessageTemplateFactory;
import com.github.taccisum.pigeon.core.repo.factory.ServiceProviderFactory;
import com.github.taccisum.pigeon.core.repo.factory.ThirdAccountFactory;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class Factory implements com.github.taccisum.domain.core.Factory {
    private PluginManager pluginManager;

    public Factory(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public MessageTemplate createMessageTemplate(long id) {
        List<MessageTemplateFactory> messageTemplateFactories = pluginManager.getExtensions(MessageTemplateFactory.class);
        messageTemplateFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (MessageTemplateFactory factory : messageTemplateFactories) {
            if (factory.match(id, new MessageTemplateFactory.Args())) {
                return factory.create(id);
            }
        }
        throw new UnsupportedOperationException("not any message template factory matched.");
    }

    @Deprecated
    public Message createMessage(long id, Message.Type type, ServiceProvider.Type spType) {
        return createMessage(id, type.name(), spType.name());
    }

    public Message createMessage(long id, String type, String spType) {
        List<MessageFactory> messageFactories = pluginManager.getExtensions(MessageFactory.class);
        messageFactories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (MessageFactory factory : messageFactories) {
            if (factory.match(id, new MessageFactory.Args(type, spType))) {
                return factory.create(id);
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
            if (factory.match(id, new ServiceProviderFactory.Args(id))) {
                return factory.create(id);
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
            if (factory.match(id, new ThirdAccountFactory.Args(spType))) {
                return factory.create(id);
            }
        }
        throw new UnsupportedOperationException("not any third account factory matched.");
    }
}
