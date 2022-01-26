package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.repo.MassTacticRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.SubMassRepo;
import com.github.taccisum.pigeon.core.service.AsyncDeliverSubMassService;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息集（代表海量消息组成的消息集合）
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public abstract class MessageMass extends Entity.Base<Long> {
    /**
     * 子集大小
     */
    private static final int SUB_MASS_SIZE = 500;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    MessageMassDAO dao;
    @Resource
    protected SubMassRepo subMassRepo;
    @Resource
    protected MessageRepo messageRepo;
    @Resource
    protected MassTacticRepo massTacticRepo;
    @Resource
    protected MessageDAO messageDAO;

    public MessageMass(Long id) {
        super(id);
    }

    public MessageMassDO data() {
        return this.dao.selectById(this.id());
    }

    /**
     * 获取此集合下的所有消息实体
     */
    public List<Message> listMessages() {
        return this.listMessages(100L);
    }

    /**
     * 获取此集合下的所有消息实体（注意：消息的数量巨大时性可能会十分低下）
     *
     * @param limit 最大数量，建议不要超过 100
     */
    public List<Message> listMessages(long limit) {
        return this.messageRepo.listByMassId(this.id(), limit);
    }

    /**
     * 投递此消息集
     */
    public final void deliver() {
        this.deliver(false);
    }

    /**
     * 投递此消息集
     *
     * @param boost 是否加速分发
     */
    public final void deliver(boolean boost) {
        // 显式指定加速，或者集合大小超过子集合 size 时强制加速分发
        boolean boost0 = boost || (this.size() > SUB_MASS_SIZE);
        this.updateStatus(Status.DELIVERING);
        this.publish(new StartDeliverEvent(boost0));
        this.doDeliver(boost);
    }

    /**
     * 执行分发
     *
     * @param boost 是否加速
     */
    protected abstract void doDeliver(boolean boost);

    /**
     * @return 集合大小
     */
    public int size() {
        return Optional.ofNullable(this.data().getSize())
                .orElse(0);
    }

    /**
     * 根据实际的消息数重新刷新统计结果，如状态、总数等（一般作为执行发生错误导致数据不准确时的补偿操作）
     */
    public void refreshStat() {
        // select count, status group by message.status
        throw new NotImplementedException();
    }

    /**
     * 批量将消息添加到此集合
     *
     * @param messages 消息
     */
    public void addAll(List<Message> messages) {
        messageDAO.updateMassIdBatch(this.id(), messages.stream()
                .map(Message::id)
                .collect(Collectors.toList())
        );
        MessageMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setSize(this.size() + messages.size());
        this.dao.updateById(o);
    }

    /**
     * 标识消息集合为已经准备完毕
     */
    public void markPrepared() {
        this.updateStatus(Status.NOT_DELIVERED);
        // TODO::
//        this.publish();
    }

    private void updateStatus(Status status) {
        MessageMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setStatus(status);
        this.dao.updateById(o);
    }

    /**
     * 获取此消息集归属的群发策略实体
     */
    public Optional<MassTactic> getTactic() {
        return this.massTacticRepo.get(this.data().getTacticId());
    }

    /**
     * 消息集全部分发完成事件
     */
    public static class DeliveredEvent extends Event.Base<MessageMass> {
    }

    /**
     * 消息集部分分发完成事件
     */
    public static class PartDeliveredEvent extends Event.Base<MessageMass> {
    }

    /**
     * 消息集状态
     */
    public enum Status {
        /**
         * 创建中
         */
        CREATING,
        /**
         * 未投递
         */
        NOT_DELIVERED,
        /**
         * 投递中
         */
        DELIVERING,
        /**
         * 已投递
         */
        ALL_DELIVERED;
    }

    public static class StartDeliverEvent extends Event.Base<MessageMass> {
        @Getter
        private boolean boost;

        public StartDeliverEvent(boolean boost) {
            this.boost = boost;
        }
    }

    public static class Default extends MessageMass {
        @Resource
        private PluginManager pluginManager;

        public Default(Long id) {
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
                // TODO:: 至少提供一个扩展点
                throw new RuntimeException("");
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

        /**
         * 将此消息集合进行分片
         *
         * @return 分片后的消息子集合
         */
        private List<SubMass> partition() {
            List<SubMass> submasses = new ArrayList<>();
            for (int i = 0; i < this.size() / SUB_MASS_SIZE; i++) {
                int left = i * SUB_MASS_SIZE;
                int right = (i + 1) * SUB_MASS_SIZE;

                SubMass sub = this.subMassRepo.create(this.id(), i, left, right, SUB_MASS_SIZE);
                submasses.add(sub);
            }
            return submasses;
        }

        void increaseCount(int successCount, int failCount, int errorCount) {
            MessageMassDO data = this.data();
            MessageMassDO o = dao.newEmptyDataObject();
            o.setId(this.id());
            o.setSuccessCount(Optional.ofNullable(data.getSuccessCount()).orElse(0) + successCount);
            o.setFailCount(Optional.ofNullable(data.getFailCount()).orElse(0) + failCount);
            o.setErrorCount(Optional.ofNullable(data.getErrorCount()).orElse(0) + errorCount);
            this.dao.updateById(o);
        }
    }
}
