package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.domain.core.exception.annotation.ErrorCode;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.dao.SubMassDAO;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.data.SubMassDO;
import com.github.taccisum.pigeon.core.entity.core.*;
import com.github.taccisum.pigeon.core.repo.MessageMassRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.utils.MagnitudeUtils;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import com.github.taccisum.pigeon.core.valueobj.Messages;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class AbstractSubMass extends Entity.Base<Long> implements SubMass {
    private static final ForkJoinPool POOL = new ForkJoinPool();
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private SubMassDAO dao;
    @Resource
    private MessageMassRepo messageMassRepo;
    @Resource
    private MessageRepo messageRepo;
    @Resource
    private MessageDAO messageDAO;

    public AbstractSubMass(Long id) {
        super(id);
    }

    @Override
    public SubMassDO data() {
        return dao.selectById(this.id());
    }

    @Override
    public int size() {
        return this.data().getSize();
    }

    @Override
    public void deliver() {
        int size = this.size();
        Integer failCount = null;

        Timer timer = Timer.builder("submass.delivery")
                .description("消息子集投递（delivery）耗费时间")
                .tag("type", this.getClass().getName())
                .tag("size", MagnitudeUtils.fromInt(size).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        try {
            failCount = timer.record(() -> {
                int maxSize = this.getMain().maxSubMassSize();
                if (size > maxSize) {
                    throw new OverMaxSizeException("SubMass", this.id(), "size", Integer.toString(size), Integer.toString(maxSize));
                }

                this.updateStatus(Status.DELIVERING);
                return this.deliver0(new Messages(this.listAllMessages()));
            });
        } finally {
            this.updateStatus(Status.DELIVERED);
            this.publish(new DeliveredEvent(failCount == null ? size : failCount));
        }
    }

    private int deliver0(Messages messages) {
        if (messages.isEmpty()) {
            log.warn("消息子集 {} size 为 {}，但实际消息数为 0，建议排查是否数据错误", this.id(), this.size());
            return 0;
        }

        if (messages.size() != this.size()) {
            log.warn("消息子集 {} size 与实际消息数 {} 不相同，建议排查是否数据错误", this.id(), messages.size());
        }

        int failCount = 0;
        RawMessageDeliverer deliverer = messages.findSuitableRawMessageDeliverer();

        if (deliverer != null) {
            MessageDO o = messageDAO.newEmptyDataObject();

            try {
                String deliveryId = deliverer.deliverBatchFast(messages.listRaw());
                if (StringUtils.isNotBlank(deliveryId)) {
                    this.recordDeliveryId(deliveryId);
                }

                if (messages.isRealTime()) {
                    o.setStatus(Message.Status.SENT);
                } else {
                    o.setStatus(Message.Status.DELIVERED);
                }
            } catch (Exception e) {
                o.setStatus(Message.Status.FAIL);
                // rethrow
                throw e;
            } finally {
                messageDAO.updateBatchByIdList(o, messages.ids());
            }

        } else {
            log.warn("消息类型 {} 无合适的 Raw 消息分发器，无法执行批量发送，将回退到原始的逐条发送方式（注意：此方式性能可能会极为低下！）", messages.getType().getName());

            try {
                failCount = POOL.submit(
                        () -> messages.getOriginList().parallelStream()
                                .map(message -> {
                                    try {
                                        message.deliver();
                                        return true;
                                    } catch (Exception e) {
                                        log.error(String.format("消息 %d 发送失败", message.id()), e);
                                        return false;
                                    }
                                })
                                .map(success -> !success ? 1 : 0)
                                .reduce(Integer::sum)
                                .orElse(0)
                ).get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO::
                e.printStackTrace();
            }
        }

        return failCount;
    }

    private void recordDeliveryId(String deliveryId) {
        SubMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setDeliveryId(deliveryId);
        this.dao.updateById(o);

    }

    private void updateStatus(Status status) {
        SubMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setStatus(status);
        this.dao.updateById(o);
    }

    @Override
    public List<Message> listAllMessages() {
        return messageRepo.listBySubMassId(this.id());
    }

    @Override
    public PartitionMessageMass getMain() {
        return (PartitionMessageMass) messageMassRepo.get(this.data().getMainId())
                .orElseThrow(() -> new DataErrorException("子集", this.id(), "所属消息集合不存在"));
    }

    @Override
    public void prepare() {
        SubMassDO data = this.data();

        Timer timer = Timer.builder("submass.preparation")
                .description("消息子集 prepare 耗费时间")
                .tag("type", this.getClass().getName())
                .tag("size", MagnitudeUtils.fromInt(this.data().getSize()).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        timer.record(() -> {
            PartitionMessageMass main = this.getMain();
            MassTactic tactic = main.getTactic().orElseThrow(() -> new DataErrorException("消息子集", this.id(), "关联策略不存在"));
            MessageTemplate template = tactic.getMessageTemplate();
            List<MessageDO> messages = new ArrayList<>();
            for (MessageInfo info : tactic.listMessageInfos(data.getStart(), data.getEnd())) {
                try {
                    MessageDO message;
                    if (info.getAccount() instanceof User) {
                        message = template.initMessageInMemory(info.getSender(), (User) info.getAccount(), info.getParams(), info.getSignature(), info.getExt());
                    } else {
                        message = template.initMessageInMemory(info.getSender(), (String) info.getAccount(), info.getParams(), info.getSignature(), info.getExt());
                    }
                    message.setStatus(Message.Status.NOT_SEND);
                    message.setMassId(main.id());
                    message.setSubMassId(this.id());
                    messages.add(message);
                } catch (DomainException e) {
                    log.warn("发送至 {} 的消息（sub mass id: {}）创建失败：{}", info, this.id(), e.getMessage());
                }
            }
            this.insertAllAndAdd(messages);

            this.markPrepared();
        });
    }

    private void insertAllAndAdd(List<MessageDO> messages) {
        Timer timer = Timer.builder("message.creation.batch")
                .tag("size", MagnitudeUtils.fromInt(messages.size()).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);
        timer.record(() -> {
            messageDAO.insertAll(messages);
        });
    }

    private void addAll(List<Message> messages) {
        messageDAO.updateMassIdBatch(this.getMain().id(), this.id(), messages.stream()
                .map(message -> message.id())
                .collect(Collectors.toList())
        );
    }

    private void markPrepared() {
        // TODO::
    }

    public static class Default extends AbstractSubMass {
        public Default(Long id) {
            super(id);
        }
    }

    @ErrorCode(value = "SUB_MASS_OVER_MAX_SIZE", inherited = true)
    public static class OverMaxSizeException extends DataErrorException {
        public OverMaxSizeException(String key, Object id, String field, String errVal, String expectVal) {
            super(key, id, field, errVal, expectVal);
        }
    }
}
