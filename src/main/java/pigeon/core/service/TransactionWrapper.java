package pigeon.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * 允许对行为进行不同的事务封装，以解决并发 & 异步逻辑中因事务导致的诸如数据可读性问题
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Service
class TransactionWrapper {

    /**
     * 在 {@link Propagation#REQUIRES_NEW} 传播行为中执行此动作
     *
     * @param run 要执行的动作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitNew(Runnable run) {
        run.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T submitNew(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * 在 {@link Propagation#NESTED} 传播行为中执行此动作
     *
     * @param run 要执行的动作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitNested(Runnable run) {
        run.run();
    }

    /**
     * 在 {@link Propagation#NESTED} 传播行为中执行此动作
     *
     * @param supplier 要执行的动作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T submitNested(Supplier<T> supplier) {
        return supplier.get();
    }
}
