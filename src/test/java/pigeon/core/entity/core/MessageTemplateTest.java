package pigeon.core.entity.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import pigeon.core.dao.MessageDAO;
import pigeon.core.data.MessageDO;
import pigeon.core.data.MessageTemplateDO;
import pigeon.core.repo.MessageRepo;
import pigeon.core.utils.JsonUtils;
import pigeon.core.valueobj.MessageInfo;
import pigeon.core.valueobj.Source;

import java.util.List;

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
        template.messageDAO = mock(MessageDAO.class);
        JsonUtils.setObjectMapper(new ObjectMapper());
    }

    @Nested
    @DisplayName("#initMessage(...)")
    class InitMessageTest {
        @Test
        @DisplayName("index")
        void index() {
            Message message = mock(Message.class);
            Mockito.doReturn(mock(MessageTemplateDO.class)).when(template).data();
            doReturn(new MessageTest.MessageDOImpl()).when(template.messageDAO).newEmptyDataObject();
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

    @Nested
    @DisplayName("#resolve(...)")
    class ResolveTargetResourceTest {
        @Test
        @DisplayName("index")
        void index() {
            List<MessageInfo> targets = template.resolve(new Source.Text("mail\n123\n456"));
            assertThat(targets.size()).isEqualTo(2);
            assertThat(targets.get(0).getAccount()).isEqualTo("123");
            assertThat(targets.get(1).getAccount()).isEqualTo("456");
        }

        @Test
        @DisplayName("转换失败时忽略掉该行")
        void ignoreIfFailToMap() {
            List<MessageInfo> targets = template.resolve(new Source.Text("mail\n123\n  \n456"));
            assertThat(targets.size()).isEqualTo(2);
        }
    }

    static class FooTemplate extends MessageTemplate {
        public FooTemplate(Long id) {
            super(id);
        }

        @Override
        public String getMessageType() {
            return "TEST";
        }

        @Override
        protected MessageInfo map(CSVRecord row, MessageInfo def) {
            String account = row.get(0);
            if (StringUtils.isBlank(account)) {
                return null;
            }
            return new MessageInfo()
                    .setAccount(account)
                    ;
        }

        @Override
        protected String getAccountHeaderName() {
            throw new NotImplementedException();
        }
    }
}