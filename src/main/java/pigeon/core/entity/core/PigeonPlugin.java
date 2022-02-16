package pigeon.core.entity.core;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class PigeonPlugin extends SpringPlugin {
    public PigeonPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected ApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setClassLoader(getWrapper().getPluginClassLoader());
        context.register(this.getSpringConfigurationClass());
        context.refresh();
        return context;
    }

    protected abstract Class<?> getSpringConfigurationClass();
}
