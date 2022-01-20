package com.github.taccisum.pigeon.core.service;

import lombok.Data;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.springframework.core.Ordered;

/**
 * 异步分发消息子集的领域服务
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface AsyncDeliverSubMassService extends AsyncCommandService<AsyncDeliverSubMassService.DeliverSubMassCommand>, Ordered, ExtensionPoint {
    /**
     * 发布命令
     */
    void publish(DeliverSubMassCommand command);

    /**
     * 处理命令
     */
    void handle(DeliverSubMassCommand command);

    @Data
    class DeliverSubMassCommand {
        /**
         * 消息子集 id
         */
        private Long id;

        public DeliverSubMassCommand(Long id) {
            this.id = id;
        }
    }

    @Extension
    class Default implements AsyncDeliverSubMassService {
        @Override
        public void publish(DeliverSubMassCommand command) {
            // TODO::
            this.handle(command);
        }

        @Override
        public void handle(DeliverSubMassCommand command) {
            System.out.println(command);
        }

        @Override
        public int getOrder() {
            return Integer.MIN_VALUE;
        }
    }
}
