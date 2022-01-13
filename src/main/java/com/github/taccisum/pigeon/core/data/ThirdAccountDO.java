package com.github.taccisum.pigeon.core.data;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.taccisum.pigeon.core.entity.core.ServiceProvider;
import lombok.Data;

/**
 * TODO:: enum 换成 string，以便插件扩展
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
@TableName("third_account")
public class ThirdAccountDO {
    private Long id;
    /**
     * 服务商类型
     */
    private ServiceProvider.Type spType;
    /**
     * 用户名
     */
    private String username;
    /**
     * 应用 ID
     */
    private String appId;
    /**
     * 应用 Secret
     */
    private String appSecret;
}
