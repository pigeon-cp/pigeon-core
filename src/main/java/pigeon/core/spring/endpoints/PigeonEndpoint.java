package pigeon.core.spring.endpoints;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * actuator endpoint
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Component
@ConditionalOnClass({RestControllerEndpoint.class, RequestMapping.class})
@RestControllerEndpoint(id = "pigeon")
public class PigeonEndpoint {
    @Resource
    private PluginManager pluginManager;

    @GetMapping("info")
    public Object getInfo() {
        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> plugins = new HashMap<>();
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            HashMap<Object, Object> info = new HashMap<>();
            info.put("version", plugin.getDescriptor().getVersion());
            info.put("state", plugin.getPluginState());
            List extensions = pluginManager.getExtensions(plugin.getPluginId());
            if (!CollectionUtils.isEmpty(extensions)) {
                info.put("extensions", extensions.stream()
                        .map(extension -> {
                            Map<String, Object> extInfo = new HashMap<>();
                            extInfo.put("class", extension.getClass());
                            if (extension instanceof Ordered) {
                                extInfo.put("order", ((Ordered) extension).getOrder());
                            }
                            return extInfo;
                        })
                        .collect(Collectors.toList())
                );
            }
            plugins.put(plugin.getPluginId(), info);
        }
        map.put("plugins", plugins);
        return map;
    }
}
