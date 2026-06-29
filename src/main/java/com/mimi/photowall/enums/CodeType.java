package com.mimi.photowall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
@AllArgsConstructor
public enum CodeType {

    /** 登录验证码 */
    LOGIN("LOGIN", "登录验证码"),

    /** 注册验证码 */
    REGISTER("REGISTER", "注册验证码"),

    /** 重置密码验证码 */
    RESET_PWD("RESET_PWD", "重置密码验证码"),

    /** 图形验证码 */
    CAPTCHA("CAPTCHA", "图形验证码");

    /** 类型编码 */
    private final String code;

    /** 描述 */
    private final String description;
}
