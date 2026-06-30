package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    @Schema(description = "验证码", example = "123456")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    @Schema(description = "新密码", example = "NewTest@123456")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "NewTest@123456")
    private String confirmPassword;

    @AssertTrue(message = "手机号和邮箱必须且只能填写一个")
    @Schema(hidden = true)
    public boolean isSingleTarget() {
        boolean hasPhone = phone != null && !phone.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        return hasPhone ^ hasEmail;
    }
}
