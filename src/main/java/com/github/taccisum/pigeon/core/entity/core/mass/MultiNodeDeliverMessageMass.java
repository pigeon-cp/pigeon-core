package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import com.github.taccisum.pigeon.core.service.AsyncDeliverSubMassService;
import org.pf4j.PluginManager;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

/**
 * 支持多节点分发的消息集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class MultiNodeDeliverMessageMass extends AbstractMessageMass {
    @Resource
    private PluginManager pluginManager;

    public MultiNodeDeliverMessageMass(Long id) {
        super(id);
    }

    @Override
    protected void doDeliver(boolean boost) {
        if (boost) {
            this.deliverOnMultiNode();
        } else {
            this.deliverOnLocal();
        }
    }

    /**
     * 多节点 + 多线程加速完成分发
     * TODO:: 未完成
     */
    protected void deliverOnMultiNode() {
        List<SubMass> submasses = this.partition();
        List<AsyncDeliverSubMassService> services = pluginManager.getExtensions(AsyncDeliverSubMassService.class);
        if (services == null || services.size() == 0) {
            throw new RuntimeException(String.format("应至少提供一个 %s 扩展点", AsyncDeliverSubMassService.class.getSimpleName()));
        }
        if (services.size() > 1) {
            // TODO:: optimize
            log.warn("仅允许存在一个 {} 扩展点，将使用优先级最高的扩展点", AsyncDeliverSubMassService.class.getSimpleName());
            services.sort(Comparator.comparingInt(AsyncDeliverSubMassService::getOrder));
        }
        AsyncDeliverSubMassService service = services.get(0);

        if (service != null) {
            for (SubMass submass : submasses) {
                service.publish(new AsyncDeliverSubMassService.DeliverSubMassCommand(submass.id()));
            }
        } else {
            // TODO:: do nothing but log
        }
    }

    /**
     * 在本地节点完成所有消息的分发
     */
    protected void deliverOnLocal() {
        int successCount = 0;
        int failCount = 0;
        int errorCount = 0;

        for (Message message : this.listMessages(Long.MAX_VALUE)) {
            // TODO:: 并发投递以提高性能
            try {
                boolean success = message.deliver();
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Message.DeliverException e) {
                log.warn("消息发送失败", e);
                failCount++;
            } catch (Exception e) {
                log.error("消息发送出错", e);
                // 为了确保批量发送时具有足够的可靠性，将所有单个 message 触发的 exception catch 掉
                errorCount++;
            }
        }
        this.increaseCount(successCount, failCount, errorCount);
        this.publish(new DeliveredEvent());
        // TODO:: 此处不 update，应该交由事件订阅器统一 update，这样可以与 remote deliver 保持一致
//            this.updateStatus(Status.ALL_DELIVERED);
    }
}
