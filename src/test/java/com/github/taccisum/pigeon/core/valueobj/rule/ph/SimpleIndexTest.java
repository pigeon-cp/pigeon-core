package com.github.taccisum.pigeon.core.valueobj.rule.ph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 2022/1/28
 */
class SimpleIndexTest {
    SimpleIndex rule = new SimpleIndex();

    @Test
    @DisplayName("index")
    public void index() {
        // index
        assertThat(rule.resolve(
                "亲爱的 {1} 家长您好，{2} 的课 {3} 将在 {4} 开始，为了确保正常上课，请提前到官网下载客户端，并检测设备情况",
                new Object[]{"张三", "李四", "三年高考五年模拟", "2021-12-31"}
        )).isEqualTo(
                "亲爱的 张三 家长您好，李四 的课 三年高考五年模拟 将在 2021-12-31 开始，为了确保正常上课，请提前到官网下载客户端，并检测设备情况"
        );

        // 乱序
        assertThat(rule.resolve(
                "{2}{1}",
                "张三", "李四"
        )).isEqualTo(
                "李四张三"
        );

        // 多重占位
        assertThat(rule.resolve(
                "{1}{2}{1}{2}",
                "张三", "李四"
        )).isEqualTo(
                "张三李四张三李四"
        );
    }

    @Test
    public void 变量不足时应替换多余占位符为空字符串() {
        // 缺少变量
        assertThat(rule.resolve(
                "0{1}{2}0{3}",
                "张三", "李四"
        )).isEqualTo(
                "0张三李四0"
        );
    }

    @Test
    public void 变量为Null时应被解释为空字符串() {
        assertThat(rule.resolve(
                "0{1}{2}0",
                "张三", null
        )).isEqualTo(
                "0张三0"
        );
    }

    @Test
    public void 传入变量较多时时额外的应被忽略() {
        assertThat(rule.resolve(
                "0{1}0",
                "张三", "李四"
        )).isEqualTo(
                "0张三0"
        );
    }

    @Test
    public void 变量为Null时应替换所有占位符为空字符串() {
        assertThat(rule.resolve(
                "0{1}{2}0{3}",
                null
        )).isEqualTo(
                "00"
        );
    }

    @Test
    public void 伪占位符不做任何处理() {
        assertThat(rule.resolve(
                "0{1}{a}0{3}",
                null
        )).isEqualTo(
                "0{a}0"
        );
    }

    @Test
    public void 模板中存在大括弧应不受影响() {
        assertThat(rule.resolve(
                "0123{{}}}{1}{{}{}}}{",
                "张三"
        )).isEqualTo(
                "0123{{}}}张三{{}{}}}{"
        );
    }

    @Test
    public void 可以处理超过10个占位符() {
        assertThat(rule.resolve(
                "{1}{2}{3}{4}{5}{6}{7}{8}{9}{10}{11}",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b"
        )).isEqualTo(
                "123456789ab"
        );
    }
}