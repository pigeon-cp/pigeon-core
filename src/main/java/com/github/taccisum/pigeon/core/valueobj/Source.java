package com.github.taccisum.pigeon.core.valueobj;

import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * 获取目标源输入流
     */
    InputStream getInputStream();

    /**
     * 获取数据源的所有行
     */
    List<String> listAllLines();

    /**
     * 获取 source 大小
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
            throw new NotImplementedException();
        }

        @Override
        public List<String> listAllLines() {
            throw new NotImplementedException();
        }
    }
}
