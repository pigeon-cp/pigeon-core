package com.github.taccisum.pigeon.core.repo.factory;

import com.github.taccisum.pigeon.core.entity.core.MessageTemplate;
import com.github.taccisum.pigeon.core.repo.EntityFactory;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/12
 */
public interface MessageTemplateFactory extends EntityFactory<Long, MessageTemplate, MessageTemplateFactory.Args> {
    @Data
    class Args {
    }
}
