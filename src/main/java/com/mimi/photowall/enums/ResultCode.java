package com.mimi.photowall.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回状态码枚举
 * <p>
 * 状态码规则（遵循阿里巴巴 Java 开发手册）：
 * - 成功：200
 * - 客户端错误：4xx
 * - 服务端错误：5xx
 * - 业务异常：1xxx
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未登录或 token 已过期 */
    UNAUTHORIZED(401, "未登录或 token 已过期"),

    /** 无权限访问 */
    FORBIDDEN(403, "无权限访问"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 请求方法不允许 */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /** 请求频率过高，请稍后重试 */
    TOO_MANY_REQUESTS(429, "请求频率过高，请稍后重试"),

    /** 服务器内部错误 */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /** 服务暂不可用 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    /** 业务异常 - 通用 */
    BUSINESS_ERROR(1000, "业务异常"),

    /** 数据已存在 */
    DATA_ALREADY_EXISTS(1001, "数据已存在"),

    /** 数据不存在 */
    DATA_NOT_FOUND(1002, "数据不存在"),

    /** 账号或密码错误 */
    ACCOUNT_OR_PASSWORD_ERROR(1003, "账号或密码错误"),

    /** 验证码错误 */
    VERIFICATION_CODE_ERROR(1004, "验证码错误"),

    /** 验证码已过期 */
    VERIFICATION_CODE_EXPIRED(1005, "验证码已过期"),

    /** 发送过于频繁 */
    SEND_TOO_FREQUENT(1006, "发送过于频繁"),

    /** 用户已被禁用 */
    USER_DISABLED(1007, "用户已被禁用"),

    /** 密码格式不符合要求 */
    PASSWORD_FORMAT_ERROR(1008, "密码格式不符合要求"),

    /** 用户名已存在 */
    USERNAME_ALREADY_EXISTS(1009, "用户名已存在"),

    /** 手机号已注册 */
    PHONE_ALREADY_REGISTERED(1010, "手机号已注册"),

    /** 邮箱已注册 */
    EMAIL_ALREADY_REGISTERED(1011, "邮箱已注册"),

    /** 图形验证码错误 */
    CAPTCHA_ERROR(1012, "图形验证码错误"),

    /** 图形验证码已过期 */
    CAPTCHA_EXPIRED(1013, "图形验证码已过期"),

    /** 角色编码已存在 */
    ROLE_CODE_ALREADY_EXISTS(1014, "角色编码已存在"),

    /** 角色不存在 */
    ROLE_NOT_FOUND(1015, "角色不存在"),

    /** 角色正在使用中，无法删除 */
    ROLE_IN_USE(1016, "角色正在使用中，无法删除"),

    /** 用户不存在 */
    USER_NOT_FOUND(1017, "用户不存在"),

    /** 角色分配失败 */
    ROLE_ASSIGN_FAILED(1018, "角色分配失败"),

    /** 文件不能为空 */
    FILE_EMPTY(1019, "文件不能为空"),

    /** 文件类型不支持 */
    FILE_TYPE_NOT_SUPPORTED(1020, "文件类型不支持"),

    /** 文件存储失败 */
    FILE_STORAGE_FAILED(1021, "文件存储失败"),

    /** 图片不存在 */
    PHOTO_NOT_FOUND(1022, "图片不存在"),

    /** 批量上传数量超限 */
    FILE_BATCH_COUNT_EXCEEDED(1023, "批量上传数量超限"),

    /** 文件大小超限 */
    FILE_SIZE_EXCEEDED(1024, "文件大小超限");

    /** 状态码 */
    private final int code;

    /** 提示信息 */
    private final String msg;
}
