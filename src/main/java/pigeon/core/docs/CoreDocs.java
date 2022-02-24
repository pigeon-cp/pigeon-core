package pigeon.core.docs;

import com.google.common.collect.Lists;
import org.pf4j.Extension;
import pigeon.core.entity.core.Message;

import java.util.List;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Extension
public class CoreDocs implements PluginDocs {
    @Override
    public List<String> listExtendedMessageType() {
        return Lists.newArrayList(Message.Type.MAIL, Message.Type.SMS);
    }

    @Override
    public List<String> listExtendedSpType() {
        return Lists.newArrayList("PIGEON");
    }
}
