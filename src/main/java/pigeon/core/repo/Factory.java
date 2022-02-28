package pigeon.core.repo;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pigeon.core.entity.core.*;
import pigeon.core.excp.UnsupportedExtensionsException;
import pigeon.core.repo.factory.*;

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

    public ThirdAccount createThirdAccount(long id, String username, String type, String spType) {
        return this.create(
                id,
                new ThirdAccountFactory.Criteria(username, type, spType),
                ThirdAccountFactory.class
        );
    }

    public User createUser(String id) {
        return this.create(id, new UserFactory.Criteria(), UserFactory.class);
    }

    public MassTactic createMassTactic(Long id, String type) {
        return this.create(id, new MassTacticFactory.Criteria(type), MassTacticFactory.class);
    }

    /**
     * @param id   mass id
     * @param type mass 类型
     * @deprecated {@link #createMessageMass(Long, String, String, String)}
     */
    public MessageMass createMessageMass(Long id, String type) {
        return this.createMessageMass(id, type, null, null);
    }

    /**
     * @param id          消息 id
     * @param type        消息类型
     * @param spType      服务商类型
     * @param messageType 消息类型
     * @return
     */
    public MessageMass createMessageMass(Long id, String type, String spType, String messageType) {
        return this.create(
                id,
                new MessageMassFactory.Criteria()
                        .setType(type)
                        .setSpType(spType)
                        .setMessageType(messageType),
                MessageMassFactory.class);
    }

    public SubMass createSubMessageMass(Long id, String spType, String messageType) {
        return this.create(
                id,
                new SubMassFactory.Criteria()
                        .setSpType(spType)
                        .setMessageType(messageType),
                SubMassFactory.class);
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
        F f0 = null;
        factories.sort(Comparator.comparingInt(EntityFactory::getOrder));
        for (F f : factories) {
            if (f.match(id, criteria)) {
                f0 = f;
                break;
            }
        }

        final F factory = f0;

        Timer timer = Timer.builder("factory.entity.creation")
                .description("工厂创建实体")
                .tags(
                        "via", "factory",
                        "class", type.getName(),
                        "delegate_class", factory == null ? "null" : factory.getClass().getName(),
                        "cache", "false"
                )
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        return timer.record(() -> {
            if (factory != null) {
                return factory.create(id, criteria);
            }

            throw new NoSuitableFactoryFoundException(id, criteria, type);
        });
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

        throw new NoSuitableFactoryFoundException(id, criteria, type);
    }

    @ErrorCode(value = "FACTORY", inherited = true)
    public static class FactoryExtensionsException extends UnsupportedExtensionsException {
        @Getter
        protected Object id;
        @Getter
        protected Object criteria;
        @Getter
        protected Class<? extends EntityFactory> type;

        public FactoryExtensionsException(Object id, Object criteria, Class<? extends EntityFactory> type) {
            super("");
            this.id = id;
            this.criteria = criteria;
            this.type = type;
        }

        @Override
        public String getMessage() {
            return String.format("Factory %s error for id %s, criteria: %s.", type.getSimpleName(), id, criteria);
        }
    }

    @ErrorCode(value = "CREATE_ENTITY", inherited = true)
    public static class CreateEntityException extends FactoryExtensionsException {
        @Getter
        private String pluginId;

        public CreateEntityException(String pluginId, Object id, Object criteria, Class<? extends EntityFactory> type) {
            super(id, criteria, type);
            this.pluginId = pluginId;
        }

        @Override
        public String getMessage() {
            return String.format("Factory %s of plugin %s fail to create entity for id %s, criteria: %s.", type.getSimpleName(), pluginId, id, criteria);
        }
    }

    @ErrorCode(value = "NOT_SUITABLE", inherited = true)
    public static class NoSuitableFactoryFoundException extends FactoryExtensionsException {
        public NoSuitableFactoryFoundException(Object id, Object criteria, Class<? extends EntityFactory> type) {
            super(id, criteria, type);
        }

        @Override
        public String getMessage() {
            return String.format(
                    "No any suitable factory found for id %s, criteria: %s. expected factory type: %s.",
                    id, criteria, type.getSimpleName()
            );
        }
    }
}
