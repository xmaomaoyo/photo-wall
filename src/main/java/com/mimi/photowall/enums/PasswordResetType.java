package com.mimi.photowall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 密码重置类型枚举
 */
@Getter
@AllArgsConstructor
public enum PasswordResetType {

    /** 手机号重置 */
    PHONE("phone", "手机号重置"),

    /** 邮箱重置 */
    EMAIL("email", "邮箱重置");

    /** 类型编码 */
    private final String code;

    /** 描述 */
    private final String description;
}
