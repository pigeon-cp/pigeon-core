package pigeon.core.valueobj.rule.ph;

import pigeon.core.valueobj.PlaceHolderRule;

/**
 * 此规则将不做任何处理，直接返回原内容
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class Direct implements PlaceHolderRule {
    @Override
    public String resolve(String content, Object params) {
        // 不做任何处理，直接返回
        return content;
    }
}
