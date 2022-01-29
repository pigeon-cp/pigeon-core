package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.PartitionCapable;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

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
    public void prepare() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<SubMass> partitions = this.partition();
        sw.stop();
        log.debug("消息集 {} 切片结果：size {}，耗时 {}ms", this.id(), partitions.size(), sw.getLastTaskTimeMillis());

        sw.start();
        partitions
                .stream()
                .forEach(SubMass::prepare);
        sw.stop();
        log.debug("所有切片子集均已 Prepared，耗时 {}ms，标记主消息集 {} 状态为 Prepared", sw.getLastTaskTimeMillis(), this.id());
        this.markPrepared();
    }

    @Override
    protected void doDeliver() throws DeliverException {
        // 分片进行投递
        this.partition().forEach(SubMass::deliver);
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
                // TODO:: 并行 insert 似乎会有隔离问题，注释掉观望一下先
//                .parallel()
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
