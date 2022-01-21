package com.github.taccisum.pigeon.core.entity.core;

import com.github.taccisum.pigeon.core.dao.MessageMassDAO;
import com.github.taccisum.pigeon.core.data.MessageMassDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/21
 */
class MessageMassTest {
    private MessageMass mass;
    private MessageMassDAO dao;

    @BeforeEach
    void setUp() {
        mass = spy(new MessageMass(1L));
        dao = mock(MessageMassDAO.class);
        mass.dao = dao;
    }

    @Nested
    @DisplayName("#increaseCount()")
    class IncreaseCountTest {
        @Test
        @DisplayName("index")
        void index() {
            doReturn(new Data()).when(mass).data();
            doNothing().when(dao).updateById(any());
            doReturn(new Data()).when(dao).newEmptyDataObject();
            mass.increaseCount(1, 1, 1);
            Data o = new Data();
            o.setId(mass.id());
            o.setSuccessCount(1);
            o.setFailCount(1);
            o.setErrorCount(1);
            verify(dao, times(1)).updateById(refEq(o));
        }

        @Test
        @DisplayName("数量累加而不是覆盖")
        void increase() {
            Data data = new Data();
            data.setSuccessCount(1);
            data.setFailCount(1);
            data.setErrorCount(1);
            doReturn(data).when(mass).data();
            doNothing().when(dao).updateById(any());
            doReturn(new Data()).when(dao).newEmptyDataObject();
            mass.increaseCount(1, 1, 1);
            Data o = new Data();
            o.setId(mass.id());
            o.setSuccessCount(2);
            o.setFailCount(2);
            o.setErrorCount(2);
            verify(dao, times(1)).updateById(refEq(o));
        }
    }

    @lombok.Data
    public class Data extends MessageMassDO {
        private Long id;
    }
}