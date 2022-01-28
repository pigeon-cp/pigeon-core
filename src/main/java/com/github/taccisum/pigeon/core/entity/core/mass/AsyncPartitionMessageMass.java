package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.service.AsyncDeliverSubMassService;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

/**
 * 支持分片、异步能力的消息集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class AsyncPartitionMessageMass extends PartitionMessageMass {
    @Resource
    private AsyncDeliverSubMassService asyncDeliverSubMassService;

    public AsyncPartitionMessageMass(Long id) {
        super(id);
    }

    @Override
    protected void doDeliver() throws DeliverException {
        // TODO:: robust 判断当前事务状态
        log.debug("消息集 {} 将执行异步分发，为了避免事务隔离性导致的数据读取问题，将待当前事务提交后执行", this.id());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.debug("监听到当前线程 {} 事务已提交，将执行分发", Thread.currentThread());
                partition().forEach(sub -> {
                    asyncDeliverSubMassService.publish(new AsyncDeliverSubMassService.DeliverSubMassCommand(sub.id()));
                });
            }
        });
    }
}
