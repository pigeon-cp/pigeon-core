package pigeon.core.entity.core;

import com.github.taccisum.domain.core.Entity;
import pigeon.core.repo.ThirdAccountRepo;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 服务提供商
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface ServiceProvider extends Entity<String> {
    default String getType() {
        return this.id();
    }

    /**
     * 根据 id 获取此服务商下账号
     *
     * @param accountId 账号 id
     * @return 此服务商下的账号
     */
    Optional<ThirdAccount> getAccount(Long accountId);

    /**
     * 根据 id 获取此服务商下账号
     *
     * @param accountId 账号 id
     * @return 此服务商下的账号
     * @throws ThirdAccountRepo.NotFoundException 账号不存在或不属于此 SP
     */
    default ThirdAccount getAccountOrThrow(Long accountId) throws ThirdAccountRepo.NotFoundException {
        return this.getAccount(accountId)
                .orElseThrow(() -> new ThirdAccountRepo.NotFoundException(accountId, this.getType()));
    }

    abstract class Base extends Entity.Base<String> implements ServiceProvider {
        @Resource
        private ThirdAccountRepo thirdAccountRepo;

        public Base(String id) {
            super(id);
        }

        @Override
        public Optional<ThirdAccount> getAccount(Long accountId) {
            Optional<ThirdAccount> accountOpt = thirdAccountRepo.get(accountId);
            if (accountOpt.isPresent()) {
                if (this.match(accountOpt.get())) {
                    return accountOpt;
                }
            }
            return Optional.empty();
        }

        /**
         * SP 是否匹配此第三方账号
         *
         * @param account 账号
         * @return true: 匹配，false: 不匹配
         */
        protected abstract boolean match(ThirdAccount account);
    }
}
