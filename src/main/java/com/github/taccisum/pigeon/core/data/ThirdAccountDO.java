package com.github.taccisum.pigeon.core.data;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
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
    private String spType;
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
    /**
     * access token
     */
    private String accessToken;
    /**
     * 备注
     */
    private String remark;
}
