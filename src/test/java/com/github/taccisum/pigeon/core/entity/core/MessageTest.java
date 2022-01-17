package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.pigeon.core.entity.core.sp.MessageServiceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        message = spy(new FooMessage(1L));
    }

    @Nested
    class deliver {
        @Test
        @DisplayName("index")
        void index() throws Exception {
            doNothing().when(message).doDelivery();
            doNothing().when(message).updateStatus(any());
            doNothing().when(message).publish(any());
            assertThat(message.deliver()).isTrue();
            verify(message, times(1)).doDelivery();
            verify(message, times(1)).updateStatus(Message.Status.DELIVERED);
            verify(message, times(1)).publish(any());
        }

        @Test
        @DisplayName("doDelivery() 发生任何异常均会被处理")
        void doDeliveryFail() throws Exception {
            doThrow(new RuntimeException("test")).when(message).doDelivery();
            doNothing().when(message).updateStatus(any());
            doNothing().when(message).publish(any());
            assertThat(message.deliver()).isFalse();
            verify(message, times(1)).doDelivery();
            verify(message, times(1)).updateStatus(Message.Status.FAIL);
            verify(message, times(1)).publish(any());
        }
    }

    private static class FooMessage extends Message {
        public FooMessage(Long id) {
            super(id);
        }

        @Override
        protected void doDelivery() throws Exception {

        }

        @Override
        protected MessageServiceProvider getServiceProvider() {
            return null;
        }
    }
}