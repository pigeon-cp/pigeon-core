package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.PartitionCapable;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import com.github.taccisum.pigeon.core.utils.MagnitudeUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
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
public class PartitionMessageMass extends AbstractMessageMass implements PartitionCapable {
    /**
     * 子集大小
     */
    public static final int SUB_MASS_SIZE = 500;
    private static final String TIMER_MASS_PREPARATION = "mass.preparation";

    public PartitionMessageMass(Long id) {
        super(id);
    }

    @Override
    public void prepare() {
        prepare(false);
    }

    /**
     * @param parallel 是否并发执行
     */
    public void prepare(boolean parallel) {
        Timer timer = Timer.builder(TIMER_MASS_PREPARATION)
                .description("消息集 prepare 耗费时间")
                .tag("type", this.getClass().getName())
                .tag("size", MagnitudeUtils.fromInt(this.data().getSize()).name())
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        timer.record(() -> {
            List<SubMass> partitions = this.partition();

            if (parallel) {
                // TODO:: 事务问题
                partitions.parallelStream()
                        .forEach(sub -> {
                            try {
                                sub.prepare();
                            } catch (Exception e) {
                                log.error(String.format("sub mass %d prepare 发生错误", sub.id()), e);
                            }
                        });
            } else {
                partitions.forEach(SubMass::prepare);
            }
            this.markPrepared();
        });
    }

    @Override
    public void deliver() throws DeliverException {
        deliver(false);
    }

    public void deliver(boolean parallel) throws DeliverException {
        // TODO:: duplicated code
        int size = this.size();
        Timer timer = Timer.builder("mass.delivery")
                .description("消息集投递（delivery）耗费时间")
                .tag("type", this.getClass().getName())
                .tag("size", MagnitudeUtils.fromInt(size).name())
                .tag("parallel", parallel ? "TRUE" : "FALSE")
                .publishPercentiles(0.5, 0.95)
                .register(Metrics.globalRegistry);

        timer.record(() -> {
            this.updateStatus(Status.DELIVERING);
            this.publish(new StartDeliverEvent());
            if (size <= 0) {
                log.warn("消息集 {} size 为 0，无需进行任何分发操作", this.id());
                this.markDeliveredAndPublicEvent();
            } else {
                this.doDeliver(parallel);
            }
        });
    }

    @Override
    protected void doDeliver() throws DeliverException {
        doDeliver(false);
    }

    protected void doDeliver(boolean parallel) throws DeliverException {
        // 分片进行投递
        if (parallel) {
            this.partition().parallelStream().forEach(SubMass::deliver);
        } else {
            this.partition().forEach(SubMass::deliver);
        }
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
                Date startTime = process.getStartTime();
                log.debug("检测到消息集合 {} 的所有子集均已处理完毕（起始时间：{}，总耗时 {}ms），修改状态并发布事件",
                        this.id(), startTime, System.currentTimeMillis() - startTime.getTime());
                this.markDeliveredAndPublicEvent();
            } else {
                log.warn("消息集合状态为 {}，请勿重复操作", Status.ALL_DELIVERED);
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
         * 获取起始时间
         */
        Date getStartTime();

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

            private Date startTime;
            private long massId;
            private AtomicInteger finishedSubMassCount;
            private int totalSubMassCount;

            public Local(long massId, int totalSubMassCount) {
                this.startTime = new Date();
                this.massId = massId;
                this.finishedSubMassCount = new AtomicInteger();
                this.totalSubMassCount = totalSubMassCount;
            }

            @Override
            public Date getStartTime() {
                return startTime;
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
