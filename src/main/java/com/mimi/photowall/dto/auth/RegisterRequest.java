package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 注册请求
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 16, message = "用户名不能超过16个字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,15}$", message = "用户名只能包含字母、数字、下划线，且以字母开头")
    @Schema(description = "用户名", example = "testuser")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    @Schema(description = "密码", example = "Test@123456")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "Test@123456")
    private String confirmPassword;

    /**
     * 手机号（必填）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 邮箱（选填）
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱（选填）", example = "test@example.com")
    private String email;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    @Schema(description = "验证码", example = "123456")
    private String code;
}
