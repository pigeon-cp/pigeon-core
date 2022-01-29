package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.exception.DataErrorException;
import com.github.taccisum.pigeon.core.dao.MessageDAO;
import com.github.taccisum.pigeon.core.dao.SubMassDAO;
import com.github.taccisum.pigeon.core.data.SubMassDO;
import com.github.taccisum.pigeon.core.entity.core.*;
import com.github.taccisum.pigeon.core.repo.MessageMassRepo;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.valueobj.MessageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class AbstractSubMass extends Entity.Base<Long> implements SubMass {
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
        int failCount = 0;
        try {
            List<Message> messages = this.listAllMessages();
            if (CollectionUtils.isEmpty(messages)) {
                log.warn("消息子集 {} size 为 {}，但实际消息数为 0，建议排查是否数据错误", this.id(), this.size());
                return;
            }
            if (messages.size() != this.size()) {
                log.warn("消息子集 {} size 与实际消息数 {} 不相同，建议排查是否数据错误", this.id(), messages.size());
            }

            this.updateStatus(Status.DELIVERING);
            failCount = messages.parallelStream()
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
                    .orElse(0);
        } finally {
            this.updateStatus(Status.DELIVERED);
            PartitionMessageMass mass = this.getMain();
            PartitionMessageMass.DeliverProcess process = mass.getProcess();
            process.increase();
            this.publish(new DeliveredEvent(failCount));
        }
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
        PartitionMessageMass main = this.getMain();
        MassTactic tactic = main.getTactic().orElseThrow(() -> new DataErrorException("消息子集", this.id(), "关联策略不存在"));
        MessageTemplate template = tactic.getMessageTemplate();
        List<Message> messages = new ArrayList<>();
        SubMassDO data = this.data();
        StopWatch sw = new StopWatch();

        sw.start();
        List<MessageInfo> infos = tactic.listMessageInfos(data.getStart(), data.getEnd());
        sw.stop();
        log.debug("成功获取子集 {} 关联的消息源 size: {} (start: {}, end: {})，耗时 {}ms",
                this.id(), infos.size(), data.getStart(), data.getEnd(), sw.getLastTaskTimeMillis());

        sw.start();
        for (MessageInfo info : infos) {
            try {
                if (info.getAccount() instanceof User) {
                    messages.add(template.initMessage(info.getSender(), (User) info.getAccount(), info.getParams()));
                } else {
                    messages.add(template.initMessage(info.getSender(), (String) info.getAccount(), info.getParams()));
                }
            } catch (MessageRepo.CreateMessageException e) {
                log.warn("发送至 {} 的消息（sub mass id: {}）创建失败：{}", info, this.id(), e.getMessage());
            }
        }
        this.addAll(messages);
        sw.stop();
        log.debug("子集 {} 所有消息初始化完毕，总耗时 {}ms", this.id(), sw.getLastTaskTimeMillis());

        this.markPrepared();
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
}
