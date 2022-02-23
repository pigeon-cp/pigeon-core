package pigeon.core.docs;

import lombok.Getter;
import org.pf4j.ExtensionPoint;

/**
 * 插件文档
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface PluginDocs extends ExtensionPoint {
    String getPluginId();

    abstract class Base implements PluginDocs {
        @Getter
        private String pluginId;

        public Base(String pluginId) {
            this.pluginId = pluginId;
        }
    }
}
