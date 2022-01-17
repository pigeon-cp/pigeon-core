package com.github.taccisum.pigeon.core.entity.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.taccisum.pigeon.core.data.MessageDO;
import com.github.taccisum.pigeon.core.data.MessageTemplateDO;
import com.github.taccisum.pigeon.core.repo.MessageRepo;
import com.github.taccisum.pigeon.core.utils.JsonUtils;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/17
 */
class MessageTemplateTest {
    private MessageTemplate template;
    private MessageRepo messageRepo;

    @BeforeEach
    void setUp() {
        messageRepo = mock(MessageRepo.class);
        template = spy(new FooTemplate(1L));
        template.messageRepo = messageRepo;
        JsonUtils.setObjectMapper(new ObjectMapper());
    }

    @Nested
    @DisplayName("#initMessage(...)")
    class InitMessageTest {
        @Test
        @DisplayName("index")
        void index() {
            Message message = mock(Message.class);
            doReturn(new MessageTemplateDO()).when(template).data();
            when(messageRepo.create(any())).thenAnswer((Answer<Message>) invocationOnMock -> {
                MessageDO o = invocationOnMock.getArgument(0, MessageDO.class);
                doReturn(o).when(message).data();
                return message;
            });
            User user = mock(User.class);
            when(user.getId()).thenReturn("1");
            when(user.getAccountFor(template)).thenReturn("receiver_account");
            assertThat(template.initMessage("ut_robot", user, Lists.newArrayList(1, 2, 3))).isEqualTo(message);
            MessageDO data = message.data();
            assertThat(data.getTemplateId()).isEqualTo(template.id());
            assertThat(data.getSender()).isEqualTo("ut_robot");
            assertThat(data.getTarget()).isEqualTo("receiver_account");
            assertThat(data.getTargetUserId()).isEqualTo("1");
            assertThat(data.getParams()).isEqualTo("[1,2,3]");
        }
    }

    static class FooTemplate extends MessageTemplate {
        public FooTemplate(Long id) {
            super(id);
        }

        @Override
        protected String getMessageType() {
            return "TEST";
        }
    }
}