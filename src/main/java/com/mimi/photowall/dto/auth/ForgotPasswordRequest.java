package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 忘记密码请求
 */
@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    /**
     * 手机号（手机号和邮箱二选一）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 邮箱（手机号和邮箱二选一）
     */
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;
}
