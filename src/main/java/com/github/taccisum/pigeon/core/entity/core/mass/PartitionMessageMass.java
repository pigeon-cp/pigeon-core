package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.entity.core.PartitionCapable;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 支持分片的消息集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public abstract class PartitionMessageMass extends AbstractMessageMass implements PartitionCapable {
    /**
     * 子集大小
     */
    public static final int SUB_MASS_SIZE = 500;

    public PartitionMessageMass(Long id) {
        super(id);
    }

    @Override
    public void deliver(boolean boost) {
        if (this.size() > SUB_MASS_SIZE) {
            log.info("消息集 {} size 大于 {}，将强制执行 boost 分发", this.id(), SUB_MASS_SIZE);
            super.deliver(true);
        } else {
            super.deliver(boost);
        }
    }

    @Override
    protected void doDeliver(boolean boost) {
        if (boost) {
            this.deliverOnPartitions();
        } else {
            this.deliverOnLocal();
        }
    }

    /**
     * 分片进行投递
     */
    void deliverOnPartitions() {
        this.partition()
                .forEach(this::deliverSubMass);
    }

    /**
     * 在本地节点完成所有消息的分发
     */
    void deliverOnLocal() {
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

    /**
     * 将此消息集合进行分片
     *
     * @return 分片后的消息子集合
     */
    @Override
    public List<SubMass> partition() {
        if (this.size() <= 0) {
            return new ArrayList<>();
        }

        List<SubMass> ls = this.subMassRepo.listByMainId(this.id());
        if (!CollectionUtils.isEmpty(ls)) {
            // 避免重复分片
            return ls;
        }

        int total = this.size();
        if (total <= 0) {
            log.warn("消息集 {} size 为 0，无需进行分片", this.id());
            return new ArrayList<>();
        }
        return Arrays.stream(partition(total))
                .parallel()
                .map(part -> {
                    int serialNum = part[0];
                    int size = part[1];
                    int startIndex = serialNum * SUB_MASS_SIZE;
                    return this.subMassRepo.create(this.id(), serialNum, startIndex, size);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取最大子集大小
     */
    int maxSubSize() {
        return SUB_MASS_SIZE;
    }

    static int[][] partition(int total) {
        if (total <= 0) {
            return new int[0][2];
        }
        int rest = total % SUB_MASS_SIZE;
        int size = rest == 0 ? total / SUB_MASS_SIZE : total / SUB_MASS_SIZE + 1;
        int[][] indexes = new int[size][2];

        for (int i = 0; i < indexes.length; i++) {
            indexes[i][0] = i;
            indexes[i][1] = SUB_MASS_SIZE;
        }

        if (rest > 0) {
            indexes[indexes.length - 1][1] = rest;
        }
        return indexes;
    }

    protected abstract void deliverSubMass(SubMass sub);


    /**
     * 标记集合为部分投递完成（若 sub 为最后一个子集，则触发 {@link DeliveredEvent} 事件）
     *
     * @param sub       子集
     * @param failCount 失败数量
     */
    public void markPartDelivered(SubMass sub, int failCount) {
        this.updateStatus(Status.DELIVERING);
        // TODO:: record error count
        this.increaseCount(sub.size() - failCount, failCount, 0);
        PartitionMessageMass.DeliverProcess process = this.getProcess();
        if (process.isFinished()) {
            if (this.data().getStatus() != Status.ALL_DELIVERED) {
                log.info("消息集合 {} 的所有子集均已处理完毕，修改状态并发布事件", this.id());
                this.markDeliveredAndPublicEvent();
            }
        }
    }

    /**
     * 获取当前消息集的分发进度
     */
    public DeliverProcess getProcess() {
        DeliverProcess.Local process = DeliverProcess.Local.PROCESSES.get(this.id());
        if (process == null) {
            synchronized (this) {
                // 避免初始化这段时间被其它线程创建
                if (process == null) {
                    process = new DeliverProcess.Local(this.id(), this.partition().size());
                    DeliverProcess.Local.PROCESSES.put(this.id(), process);
                }
            }
        }
        return process;
    }

    public interface DeliverProcess {
        /**
         * 进度 +1
         */
        void increase();

        /**
         * 判断进度是否 100%
         */
        boolean isFinished();

        class Local implements DeliverProcess {
            private static final Map<Long, Local> PROCESSES = new HashMap();

            private long massId;
            private AtomicInteger finishedSubMassCount;
            private int totalSubMassCount;

            public Local(long massId, int totalSubMassCount) {
                this.massId = massId;
                this.finishedSubMassCount = new AtomicInteger();
                this.totalSubMassCount = totalSubMassCount;
            }

            @Override
            public void increase() {
                int now = this.finishedSubMassCount.incrementAndGet();
                if (now > totalSubMassCount) {
                    this.finishedSubMassCount.decrementAndGet();
                }
            }

            @Override
            public boolean isFinished() {
                return finishedSubMassCount.get() == totalSubMassCount;
            }
        }
    }
}
