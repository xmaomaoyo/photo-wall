package com.mimi.photowall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 */
@Getter
@AllArgsConstructor
public enum LoginType {

    /** 手机验证码登录 */
    SMS(1, "手机验证码登录"),

    /** 邮箱验证码登录 */
    EMAIL(2, "邮箱验证码登录"),

    /** 密码登录 */
    PASSWORD(3, "密码登录");

    /** 登录类型 */
    private final int type;

    /** 描述 */
    private final String description;
}
