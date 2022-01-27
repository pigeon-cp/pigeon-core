package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.entity.core.SubMass;
import com.github.taccisum.pigeon.core.service.AsyncDeliverSubMassService;

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
    protected void deliverSubMass(SubMass sub) {
        asyncDeliverSubMassService.publish(new AsyncDeliverSubMassService.DeliverSubMassCommand(sub.id()));
    }
}
