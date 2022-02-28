package pigeon.core.entity.core;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pigeon.core.dao.MessageDAO;
import pigeon.core.entity.core.sp.MessageServiceProvider;
import pigeon.core.impl.dao.data.MessageDOImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/17
 */
@ExtendWith(SpringExtension.class)
class MessageTest {
    private Message message;

    @BeforeEach
    void setUp() {
        this.message = spy(new FooMessage(1L));
        this.message.dao = mock(MessageDAO.class);
        when(this.message.dao.newEmptyDataObject()).thenReturn(new MessageDOImpl());
    }

    @Nested
    @DisplayName("#deliver()")
    class deliver {
        @Test
        @DisplayName("index")
        void index() throws Exception {
            doReturn(new MessageDOImpl()).when(message).data();
            doNothing().when(message).doDelivery();
            doNothing().when(message).updateStatus(any(), any());
            doNothing().when(message).publish(any());
            assertThat(message.deliver()).isTrue();
            verify(message, times(1)).doDelivery();
            verify(message, times(1)).updateStatus(eq(Message.Status.DELIVERED), any());
            verify(message, times(1)).publish(any());
        }

        @Test
        @DisplayName("doDelivery() 发生任何异常均会被处理")
        void doDeliveryFail() throws Exception {
            doReturn(new MessageDOImpl()).when(message).data();
            doThrow(new RuntimeException("test")).when(message).doDelivery();
            doNothing().when(message).updateStatus(any(), any());
            doNothing().when(message).publish(any());
            assertThat(message.deliver()).isFalse();
            verify(message, times(1)).doDelivery();
            verify(message, times(1)).updateStatus(eq(Message.Status.FAIL), any());
            verify(message, times(1)).publish(any());
        }

        @Test
        @DisplayName("必须关联模板时 template id 不能为空")
        void shouldHaveTemplateId() throws Exception {
            doReturn(new MessageDOImpl()).when(message).data();
            doReturn(true).when(message).shouldRelateTemplate();

            Assert.assertThrows(Message.DeliverException.class, () -> {
                message.deliver();
            });
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("实时发送的消息直接标记发送结果")
        void markSentIfRealTimeMessage(boolean success) throws Exception {
            doReturn(new MessageDOImpl()).when(message).data();
            doReturn(true).when(message).isRealTime();

            if (success) {
                doNothing().when(message).doDelivery();
                doNothing().when(message).markSent(anyBoolean());
                message.deliver();
                verify(message, times(1)).doDelivery();
                verify(message, times(1)).markSent(true);
            } else {
                doThrow(new RuntimeException()).when(message).doDelivery();
                doNothing().when(message).markSent(anyBoolean(), any());
                message.deliver();
                verify(message, times(1)).doDelivery();
                verify(message, times(1)).markSent(eq(false), anyString());
            }
        }
    }

    private static class FooMessage extends Message {
        public FooMessage(Long id) {
            super(id);
        }

        @Override
        public boolean isRealTime() {
            return false;
        }

        @Override
        protected void doDelivery() throws Exception {

        }

        @Override
        public MessageServiceProvider getServiceProvider() {
            return null;
        }
    }
}