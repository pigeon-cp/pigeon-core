package pigeon.core.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/21
 */
class CSVUtilsTest {
    private CSVFormat format = CSVFormat.DEFAULT
            .builder().setHeader().setSkipHeaderRecord(true)
            .build();

    @Test
    @DisplayName("index")
    void index() throws IOException {
        List<CSVRecord> rows = parseStr("mail\n12345").getRecords();
        assertThat(rows.size()).isEqualTo(1);
        CSVRecord row = rows.get(0);
        assertThat(row.get("mail")).isEqualTo("12345");
    }

    @Nested
    @DisplayName("#getOrDefault(...)")
    class GetOrDefault {
        @Test
        @DisplayName("index")
        void index() throws IOException {
            CSVRecord row = parseStr("mail\n12345").getRecords().get(0);
            assertThat(CSVUtils.getOrDefault(row, "mail")).isEqualTo("12345");
        }

        @Test
        @DisplayName("fallback（header -> index -> def）")
        void fallback() throws IOException {
            CSVRecord row = parseStr("mail0\n12345").getRecords().get(0);
            assertThat(CSVUtils.getOrDefault(row, "mail", 0)).isEqualTo("12345");
            assertThat(CSVUtils.getOrDefault(row, "mail", -1, "fb_def")).isEqualTo("fb_def");
            assertThat(CSVUtils.getOrDefault(row, "mail", 1, "fb_def")).isEqualTo("fb_def");
            assertThat(CSVUtils.getOrDefault(row, "mail", 1, null)).isEqualTo(null);
        }
    }

    private CSVParser parseStr(String s) {
        try {
            return format.parse(new BufferedReader(new StringReader(s)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}