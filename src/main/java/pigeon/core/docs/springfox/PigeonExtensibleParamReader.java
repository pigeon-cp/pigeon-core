package pigeon.core.docs.springfox;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import org.pf4j.PluginManager;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import pigeon.core.docs.PluginDocs;
import springfox.documentation.schema.Annotations;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import javax.annotation.Resource;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展 pigeon 插件的参数描述
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Component
public class PigeonExtensibleParamReader implements ParameterBuilderPlugin, ModelPropertyBuilderPlugin {
    @Resource
    private PluginManager pluginManager;

    @Override
    public void apply(ParameterContext context) {
        Optional<Extensible> extensible = context.resolvedMethodParameter().findAnnotation(Extensible.class);

        if (extensible.isPresent()) {
            if (context.resolvedMethodParameter().getParameterType().isInstanceOf(String.class)) {
                context.parameterBuilder()
                        .allowableValues(new AllowableListValues(this.listAvailableValues(extensible.get()), "String"));
            }
        }
    }

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<Extensible> annotation = Optional.absent();

        if (context.getAnnotatedElement().isPresent()) {
            annotation = annotation.or(findApiModePropertyAnnotation(context.getAnnotatedElement().get()));
        }

        Optional<BeanPropertyDefinition> beanPropertyDefinition = context.getBeanPropertyDefinition();
        if (beanPropertyDefinition.isPresent()) {
            annotation = annotation.or(Annotations.findPropertyAnnotation(beanPropertyDefinition.get(), Extensible.class));
            if (beanPropertyDefinition.get().getRawPrimaryType().isAssignableFrom(String.class) && annotation.isPresent()) {
                context.getBuilder()
                        .allowableValues(new AllowableListValues(this.listAvailableValues(annotation.get()), "String"));
            }
        }

    }

    private List<String> listAvailableValues(Extensible annotation) {
        List<PluginDocs> docs = pluginManager.getExtensions(PluginDocs.class);
        List<String> availableValues = docs.stream()
                .map(doc -> {
                    switch (annotation.value()) {
                        case MESSAGE_TYPE:
                            return doc.listExtendedMessageType();
                        case SERVICE_PROVIDER_TYPE:
                            return doc.listExtendedSpType();
                        default:
                            return null;
                    }
                })
                .filter(ls -> ls != null)
                .flatMap(ls -> ls.stream())
                .distinct()
                .collect(Collectors.toList());
        return availableValues;
    }

    static Optional<Extensible> findApiModePropertyAnnotation(AnnotatedElement annotated) {
        Optional<Extensible> annotation = Optional.absent();

        if (annotated instanceof Method) {
            // If the annotated element is a method we can use this information to check superclasses as well
            annotation = Optional.fromNullable(AnnotationUtils.findAnnotation(((Method) annotated), Extensible.class));
        }

        return annotation.or(Optional.fromNullable(AnnotationUtils.getAnnotation(annotated, Extensible.class)));
    }


    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
