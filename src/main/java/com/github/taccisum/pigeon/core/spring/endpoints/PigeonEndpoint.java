package com.github.taccisum.pigeon.core.spring.endpoints;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.HashMap;

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
            plugins.put(plugin.getPluginId(), info);
        }
        map.put("plugins", plugins);
        return map;
    }
}
