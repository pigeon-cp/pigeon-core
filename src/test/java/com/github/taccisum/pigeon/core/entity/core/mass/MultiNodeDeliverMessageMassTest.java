package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.data.MessageMassDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/27
 */
class MultiNodeDeliverMessageMassTest {
    private MultiNodeDeliverMessageMass mass;

    @BeforeEach
    void setUp() {
        mass = spy(new MultiNodeDeliverMessageMass(1L));
    }

    @Nested
    @DisplayName("#doDelivery()")
    class DoDeliveryTest {
        @Test
        @DisplayName("index0")
        void index() {
            doNothing().when(mass).deliverOnMultiNode();
            mass.doDeliver(true);
            verify(mass, times(1)).deliverOnMultiNode();
            verify(mass, times(0)).deliverOnLocal();
        }

        @Test
        @DisplayName("index1")
        void index1() {
            doNothing().when(mass).deliverOnLocal();
            mass.doDeliver(false);
            verify(mass, times(0)).deliverOnMultiNode();
            verify(mass, times(1)).deliverOnLocal();
        }
    }

    @lombok.Data
    public class Data extends MessageMassDO {
        private Long id;
    }
}