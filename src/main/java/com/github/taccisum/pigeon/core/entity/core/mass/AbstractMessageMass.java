package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.MessageMass;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import com.github.taccisum.pigeon.core.repo.MassTacticRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.repo.SubMassRepo;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public final void deliver(boolean boost) {
        // 显式指定加速，或者集合大小超过子集合 size 时强制加速分发
        boolean boost0 = boost || (this.size() > SUB_MASS_SIZE);
        this.updateStatus(Status.DELIVERING);
        this.publish(new StartDeliverEvent(boost0));
        this.doDeliver(boost);
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
//        this.publish();
    }

    @Override
    public void markDelivered() {
        this.updateStatus(Status.ALL_DELIVERED);
    }

    private void updateStatus(Status status) {
        MessageMassDO o = dao.newEmptyDataObject();
        o.setId(this.id());
        o.setStatus(status);
        this.dao.updateById(o);
    }

    @Override
    public Optional<MassTactic> getTactic() {
        return this.massTacticRepo.get(this.data().getTacticId());
    }

    /**
     * 将此消息集合进行分片
     *
     * @return 分片后的消息子集合
     */
    protected List<SubMass> partition() {
        List<SubMass> submasses = new ArrayList<>();
        for (int i = 0; i < this.size() / SUB_MASS_SIZE; i++) {
            int left = i * SUB_MASS_SIZE;
            int right = (i + 1) * SUB_MASS_SIZE;

            SubMass sub = this.subMassRepo.create(this.id(), i, left, right, SUB_MASS_SIZE);
            submasses.add(sub);
        }
        return submasses;
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
