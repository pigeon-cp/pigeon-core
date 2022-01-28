package com.github.taccisum.pigeon.core.entity.core.mass;

import com.github.taccisum.pigeon.core.data.MessageMassDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/27
 */
class PartitionMessageMassTest {
    private PartitionMessageMass mass;

    @BeforeEach
    void setUp() {
        mass = spy(new PartitionMessageMass(1L) {
        });
    }

    @Nested
    @DisplayName("#partition()")
    static class PartitionTest {
        @Test
        @DisplayName("分片数量应正确")
        void partition() {
            assertThat(PartitionMessageMass.partition(-1).length).isEqualTo(0);
            assertThat(PartitionMessageMass.partition(0).length).isEqualTo(0);
            assertThat(PartitionMessageMass.partition(1).length).isEqualTo(1);
            assertThat(PartitionMessageMass.partition(499).length).isEqualTo(1);
            assertThat(PartitionMessageMass.partition(501).length).isEqualTo(2);
            assertThat(PartitionMessageMass.partition(1000 * 1000).length).isEqualTo(2000);
            assertThat(PartitionMessageMass.partition(1000 * 1000 + 1).length).isEqualTo(2001);
        }

        @ParameterizedTest
        @DisplayName("每个分片的下标及大小应正确")
        @MethodSource("indexAndSizeSource")
        void indexAndSize(int total, int subMax, int rest) {
            int[][] parts = PartitionMessageMass.partition(total);
            if (total <= 0) {
                assertThat(parts.length).isEqualTo(0);
                return;
            }
            if (rest != 0) {
                assertThat(parts[parts.length - 1][1]).isEqualTo(rest);
            } else {
                assertThat(parts[parts.length - 1][1]).isEqualTo(subMax);
            }
        }

        static Object[] indexAndSizeSource() {
            return new Object[]{
                    create(-1),
                    create(0),
                    create(1),
                    create(499),
                    create(501),
                    create(1000 * 1000),
                    create(1000 * 1000 + 1),
                    create(1000 * 1000 + 499),
            };
        }

        private static Integer[] create(int total) {
            return new Integer[]{total, PartitionMessageMass.SUB_MASS_SIZE, total % PartitionMessageMass.SUB_MASS_SIZE};
        }
    }

    @lombok.Data
    public class Data extends MessageMassDO {
        private Long id;
    }
}