package pigeon.core.service;

import com.github.taccisum.domain.core.exception.DataNotFoundException;
import pigeon.core.repo.SubMassRepo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 异步分发消息子集的领域服务
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface AsyncDeliverSubMassService extends AsyncCommandService<AsyncDeliverSubMassService.DeliverSubMassCommand> {
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

    @Slf4j
    class Default implements AsyncDeliverSubMassService {
        private SubMassRepo subMassRepo;

        public Default(SubMassRepo subMassRepo) {
            this.subMassRepo = subMassRepo;
        }

        @Override
        public void publish(DeliverSubMassCommand command) {
            CompletableFuture.runAsync(() -> {
                // 直接转交处理，异步执行
                try {
                    // sleep 1s，避免发布方事务未提交导致 handle 时查询不到子集合
                    Thread.sleep(1000L);
                    this.handle(command);
                } catch (Exception e) {
                    log.warn(String.format("异步命令 %s 执行发生异常", command), e);
                }
            });
        }

        @Override
        public void handle(DeliverSubMassCommand command) {
            this.subMassRepo.get(command.getId())
                    .orElseThrow(() -> new DataNotFoundException("子集合", command.getId()))
                    .deliver();
        }
    }
}
