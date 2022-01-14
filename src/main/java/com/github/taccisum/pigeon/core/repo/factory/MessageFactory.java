package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.Message;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/12
 */
public interface MessageFactory extends EntityFactory<Long, Message, MessageFactory.Args>  {
    @Data
    public static class Args {
        private String type;
        private String spType;

        public Args(String type, String spType) {
            this.type = type;
            this.spType = spType;
        }
    }
}
