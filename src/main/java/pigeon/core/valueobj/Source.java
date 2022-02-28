package pigeon.core.valueobj;

import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 群发策略数据源
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
public interface Source {
    /**
     * @return 目标源输入流
     * TODO:: throw IOException
     */
    InputStream getInputStream();

    /**
     * @return 数据源的所有行
     * @deprecated 文件太大时可能会导致内存泄漏，后面看看要不要删掉这个方法，或者做控制（比如只读 head x 行）
     */
    List<String> listAllLines();

    /**
     * @return 目标源 size
     */
    int size();

    abstract class Base implements Source {
        @Getter
        private String source;

        public Base(String source) {
            this.source = source;
        }

        @Override
        public int size() {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getInputStream()));
            // TODO:: cast int
            // TODO:: handle head
            return (int) br.lines().count() - 1;
        }
    }

    class Text extends Base {
        public Text(String source) {
            super(source);
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.getSource().getBytes());
        }

        @Override
        public List<String> listAllLines() {
            return Arrays.stream(this.getSource().split("\n"))
                    .collect(Collectors.toList());
        }
    }

    class File extends Base {
        public File(String fileName) {
            super(fileName);
        }

        @Override
        public InputStream getInputStream() {
            throw new NotImplementedException();
        }

        @Override
        public List<String> listAllLines() {
            throw new NotImplementedException();
        }
    }

    class Url extends Base {
        public Url(String url) {
            super(url);
        }

        @Override
        public InputStream getInputStream() {
            // check 缓存，不存在则从网络下载

            java.io.File csv = new java.io.File("/tmp/pigeon/cache/source/1.csv");
            try {
                return new FileInputStream(csv);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<String> listAllLines() {
            throw new NotImplementedException();
        }
    }
}
