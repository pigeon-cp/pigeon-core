package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import com.github.taccisum.pigeon.core.repo.MassTacticRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.SubMassRepo;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
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
    MessageMassDAO dao;
    @Resource
    protected SubMassRepo subMassRepo;
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
    public void deliver(boolean boost) {
        this.updateStatus(Status.DELIVERING);
        this.publish(new StartDeliverEvent(boost));
        if (this.size() <= 0) {
            log.warn("消息集 {} size 为 0，无需进行任何分发操作", this.id());
            this.markDeliveredAndPublicEvent();
        } else {
            this.doDeliver(boost);
        }
    }

    protected abstract void doDeliver(boolean boost);

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
}
