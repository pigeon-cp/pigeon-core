package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import com.github.taccisum.domain.core.Event;
import com.github.taccisum.domain.core.EventPublisher;
import com.github.taccisum.pigeon.core.data.SubMassDO;
import lombok.Getter;

import java.util.List;

/**
 * 消息子集
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface SubMass extends Entity<Long>, EventPublisher {
    SubMassDO data();

    /**
     * 获取集合大小
     */
    int size();

    /**
     * 执行投递前准备工作
     */
    void prepare();

    /**
     * 投递此消息子集
     */
    void deliver();

    /**
     * 获取子集下的所有消息
     */
    List<Message> listAllMessages();

    /**
     * 获取当前子集所属的消息集
     */
    MessageMass getMain();

    enum Status {
        /**
         * 初始
         */
        INIT,
        /**
         * 投递中
         */
        DELIVERING,
        /**
         * 已投递
         */
        DELIVERED
    }

    /**
     * 子集分发完成事件
     */
    class DeliveredEvent extends Event.Base<SubMass> {
        @Getter
        private int failCount;

        public DeliveredEvent(int failCount) {
            this.failCount = failCount;
        }
    }
}
