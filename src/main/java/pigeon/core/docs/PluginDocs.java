package pigeon.core.docs;

import org.pf4j.ExtensionPoint;

import java.util.List;

/**
 * 插件文档
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface PluginDocs extends ExtensionPoint {
    /**
     * 返回扩展的消息类型
     */
    default List<String> listExtendedMessageType() {
        return null;
    }

    /**
     * 返回扩展的服务提供商类型
     */
    default List<String> listExtendedSpType() {
        return null;
    }
}
