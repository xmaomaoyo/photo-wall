package com.mimi.photowall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 * 对应数据库login_log表
 */
@Data
@TableName("login_log")
public class LoginLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（登录失败时可能为空）
     */
    private Long userId;

    /**
     * 登录类型：1-手机验证码，2-邮箱验证码，3-密码
     */
    private Integer loginType;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
