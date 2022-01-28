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
@TableName("message_template")
public class MessageTemplateDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 模板消息类型
     */
    private String type;
    /**
     * 模板占位规则类型
     */
    private String placeholderRule;
    /**
     * 模板所关联的第三方服务商
     */
    private String spType;
    /**
     * 模板所关联的三方服务商账号 id
     */
    private Long spAccountId;
    /**
     * 此消息模板所关联的在第三方服务商处维护的编码
     */
    private String thirdCode;
    /**
     * 模板标题
     */
    private String title;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 发送时使用的标签
     */
    private String tag;
}
