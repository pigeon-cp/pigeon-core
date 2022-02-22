package pigeon.core.entity.core.mass;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pigeon.core.dao.MessageDAO;
import pigeon.core.dao.MessageMassDAO;
import pigeon.core.data.MessageMassDO;
import pigeon.core.entity.core.*;
import pigeon.core.repo.MassTacticRepo;
import pigeon.core.repo.MessageRepo;
import pigeon.core.utils.MagnitudeUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息集基类
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class AbstractMessageMass extends Entity.Base<Long> implements MessageMass {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    protected MessageMassDAO dao;
    @Resource
    protected MessageRepo messageRepo;
    @Resource
    protected MassTacticRepo massTacticRepo;
    @Resource
    protected MessageDAO messageDAO;

    public AbstractMessageMass(Long id) {
        super(id);
    }

    @Override
    public MessageMassDO data() {
        return this.dao.selectById(this.id());
    }

    @Override
    public List<Message> listMessages(long limit) {
        return this.messageRepo.listByMassId(this.id(), limit);
    }

    @Override
    public boolean hasPrepared() {
        return !Status.CREATING.equals(this.data().getStatus());
    }

    @Override
    public void deliver() throws DeliverException {
        int size = this.size();
        LongTaskTimer timer = LongTaskTimer.builder("mass.delivery")
                .description("消息集投递（delivery）耗费时间")
                .tag("type", this.getClass().getName())
                .tag("size", MagnitudeUtils.fromInt(size).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        timer.record(() -> {
            this.updateStatus(Status.DELIVERING);
            this.publish(new StartDeliverEvent());
            if (size <= 0) {
                log.warn("消息集 {} size 为 0，无需进行任何分发操作", this.id());
                this.markDeliveredAndPublicEvent();
            } else {
                this.doDeliver();
            }
        });
    }

    protected abstract void doDeliver() throws DeliverException;

    @Override
    public int size() {
        return Optional.ofNullable(this.data().getSize())
                .orElse(0);
    }

    @Override
    public void refreshStat() {
        // select count, status group by message.status
        throw new NotImplementedException();
    }

    @Override
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

    @Override
    public void markPrepared() {
        this.updateStatus(Status.NOT_DELIVERED);
    }

    @Override
    public void markDelivered() {
        this.updateStatus(Status.ALL_DELIVERED);
    }

    protected void updateStatus(Status status) {
        MessageMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setStatus(status);
        this.dao.updateById(o);
    }

    @Override
    public Optional<MassTactic> getTactic() {
        return this.massTacticRepo.get(this.data().getTacticId());
    }

    protected void increaseCount(int successCount, int failCount, int errorCount) {
        MessageMassDO data = this.data();
        MessageMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setSuccessCount(Optional.ofNullable(data.getSuccessCount()).orElse(0) + successCount);
        o.setFailCount(Optional.ofNullable(data.getFailCount()).orElse(0) + failCount);
        o.setErrorCount(Optional.ofNullable(data.getErrorCount()).orElse(0) + errorCount);
        this.dao.updateById(o);
    }

    public static class Default extends AbstractMessageMass {
        public Default(Long id) {
            super(id);
        }

        @Override
        protected void doDeliver() throws DeliverException {
            // 本地简单遍历以进行投递
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
            this.markDeliveredAndPublicEvent();
        }

        @Override
        public void prepare() {
            // 不可分片的 mass，遍历来实现
            MassTactic tactic = this.getTactic().orElseThrow(() -> {
                return new DataErrorException("消息集合", this.id(), "关联的群发策略不存在");
            });
            MessageTemplate template = tactic.getMessageTemplate();
            List<Message> messages = tactic.listMessageInfos()
                    .parallelStream()
                    .map(info -> {
                        try {
                            if (info.getAccount() instanceof User) {
                                return template.initMessage(info.getSender(), (User) info.getAccount(), info.getParams());
                            } else {
                                return template.initMessage(info.getSender(), (String) info.getAccount(), info.getParams());
                            }
                        } catch (MessageRepo.CreateMessageException e) {
                            log.warn("发送至 {} 的消息创建失败：{}", info, e.getMessage());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            this.addAll(messages);
            this.markPrepared();
        }
    }
}
