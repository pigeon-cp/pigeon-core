package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.pigeon.core.data.MassTacticDO;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/21
 */
class MassTacticTest {
    MassTactic tactic;

    @BeforeEach
    void setUp() {
        tactic = spy(new FooTactic(1L));
    }

    @Nested
    @DisplayName("#exec(...)")
    class ExecTest {
        @Test
        @DisplayName("index")
        void index() {
            Data data = new Data();
            doReturn(data).when(tactic).data();
            MessageMass mass = mock(MessageMass.class);
            doReturn(mass).when(tactic).prepare();

            tactic.exec();
            verify(tactic, times(1)).prepare();
            verify(mass, times(1)).deliver();
        }

        @Test
        @DisplayName("未测试或未通过测试不允许执行")
        void execNotAllowIfNotTest() {
            Data data = new Data();
            doReturn(data).when(tactic).data();

            // must test but not test
            data.setMustTest(true);
            data.setHasTest(null);
            Assert.assertThrows(MassTactic.ExecException.class, () -> {
                tactic.exec();
            });

            // must test but test fail
            data.setMustTest(true);
            data.setHasTest(false);
            Assert.assertThrows(MassTactic.ExecException.class, () -> {
                tactic.exec();
            });
        }
    }

    @Nested
    @DisplayName("#prepare(...)")
    class PrepareTest {
        @Test
        @DisplayName("index")
        void index() {
            doReturn(false).when(tactic).isExecuting();
            doReturn(false).when(tactic).hasPrepared();
            doReturn(null).when(tactic).doPrepare();

            tactic.prepare();

            verify(tactic, times(1)).doPrepare();
        }

        @Test
        @DisplayName("执行中的策略不允许 prepare")
        void notAllowIfExecuting() {
            doReturn(true).when(tactic).isExecuting();

            Assert.assertThrows(MassTactic.PrepareException.class, () -> {
                tactic.prepare();
                verify(tactic, times(0)).doPrepare();
            });
        }

        @Test
        @DisplayName("已经准备好的 mass 直接返回")
        void returnPreparedMassDirectly() {
            doReturn(false).when(tactic).isExecuting();
            doReturn(true).when(tactic).hasPrepared();
            MessageMass mass = mock(MessageMass.class);
            doReturn(Optional.of(mass)).when(tactic).getPreparedMass();

            assertThat(tactic.prepare()).isEqualTo(mass);
            verify(tactic, times(0)).doPrepare();
        }
    }

    @Nested
    @DisplayName("#doPrepare(...)")
    class DoPrepare {
        @Test
        @DisplayName("index")
        void index() {
            doReturn(new Data()).when(tactic).data();
            MessageMass mass = mock(MessageMass.class);
            doReturn(mass).when(tactic).newMass();
            doReturn(mock(MessageTemplate.class)).when(tactic).getMessageTemplate();
            doReturn(new ArrayList<>()).when(tactic).listMessageInfos();
            doNothing().when(tactic).markPrepared(mass);

            tactic.doPrepare();

            verify(mass, times(1)).addAll(any());
            verify(tactic, times(1)).markPrepared(mass);
        }
    }

    @Nested
    @DisplayName("#isExecuting(...)")
    class IsExecutingTest {
        @Test
        @DisplayName("index")
        void index() {
            Data data = new Data();
            doReturn(data).when(tactic).data();

            data.setStatus(MassTactic.Status.EXECUTING);
            assertThat(tactic.isExecuting()).isTrue();
        }
    }

    @lombok.Data
    class Data extends MassTacticDO {
        private Long id;
    }

    class FooTactic extends MassTactic {
        public FooTactic(Long id) {
            super(id);
        }
    }
}