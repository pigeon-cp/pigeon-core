package com.github.taccisum.pigeon.core.entity.core;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface MessageTarget {
    String getAccountFor(MessageTemplate template);

    class Default implements MessageTarget {
        private String account;

        public Default(String account) {
            this.account = account;
        }

        @Override
        public String getAccountFor(MessageTemplate template) {
            return account;
        }
    }
}
