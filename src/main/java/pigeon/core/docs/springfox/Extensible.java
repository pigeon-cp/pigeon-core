package pigeon.core.docs.springfox;

import pigeon.core.docs.ExtensionType;

import java.lang.annotation.*;

/**
 * 标识一个参数/属性为可扩展的
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extensible {
    /**
     * 扩展类型
     */
    ExtensionType value();
}
