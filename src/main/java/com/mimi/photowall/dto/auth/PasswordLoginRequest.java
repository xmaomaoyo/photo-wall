package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 密码登录请求
 */
@Data
@Schema(description = "密码登录请求")
public class PasswordLoginRequest {

    /**
     * 账号（手机号/邮箱/用户名）
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号（手机号/邮箱/用户名）", example = "13800138000")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "Test@123456")
    private String password;

    /**
     * 图形验证码ID
     */
    @NotBlank(message = "验证码ID不能为空")
    @Schema(description = "图形验证码ID")
    private String captchaId;

    /**
     * 图形验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "图形验证码", example = "AB12")
    private String captchaCode;
}
