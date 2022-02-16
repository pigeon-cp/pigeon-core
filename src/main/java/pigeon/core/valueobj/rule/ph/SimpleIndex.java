package pigeon.core.valueobj.rule.ph;

import pigeon.core.utils.TolerantCastUtils;
import pigeon.core.valueobj.PlaceHolderRule;

/**
 * <pre>
 * 基于简单的模板规则构建消息内容（索引占位符）
 *
 * 示例：
 * template: 亲爱的 {1} 家长您好，{2} 的课 {3} 将在 {4} 开始，为了确保正常上课，请提前到官网下载客户端，并检测设备情况
 * vars: ["张三", "李四", "三年高考五年模拟", "2021-12-31"]
 * result:
 * 亲爱的 张三 家长您好，李四 的课 三年高考五年模拟 将在 2021-12-31 开始，为了确保正常上课，请提前到官网下载客户端，并检测设备情况
 *
 * 更多示例可查看相关单元测试
 * </pre>
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class SimpleIndex implements PlaceHolderRule {
    public String resolve(String content, String... params) {
        return this.resolve(content, (Object) params);
    }

    @Override
    public String resolve(String content, Object params) {
        Object[] vars = TolerantCastUtils.toArray(params);
        String tmp = content;
        if (vars != null) {
            for (int i = 0; i < vars.length; i++) {
                int index = i + 1;
                String var = vars[i] == null ? "" : vars[i].toString();
                tmp = tmp.replaceAll("\\{" + index + "\\}", var);
            }
        }
        tmp = tmp.replaceAll("\\{[0-9]+\\}", "");
        return tmp;
    }
}
