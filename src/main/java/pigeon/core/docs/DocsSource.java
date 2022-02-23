package pigeon.core.docs;

import pigeon.core.docs.desc.FactoryDesc;

/**
 * 文档源，代表实现此接口的类可以转换为一个文档描述对象 {@link Description}
 *
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public interface DocsSource<D extends Description> {
    D toDocs();

    interface Factory extends DocsSource<FactoryDesc> {
        interface Criteria extends DocsSource<FactoryDesc.CriteriaDesc> {
        }
    }
}
