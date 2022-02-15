package com.github.taccisum.pigeon.core.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Data
@TableName("third_account")
public class ThirdAccountDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 账号类型
     *
     * @since 0.2
     */
    private String type;
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
    /**
     * 拓展内容（供插件实现时自行拓展使用）
     *
     * @since 0.2
     */
    private String ext;
}
