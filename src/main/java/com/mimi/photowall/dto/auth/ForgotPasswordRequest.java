package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "忘记密码请求")
public class ForgotPasswordRequest {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    @AssertTrue(message = "手机号和邮箱必须且只能填写一个")
    @Schema(hidden = true)
    public boolean isSingleTarget() {
        boolean hasPhone = phone != null && !phone.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        return hasPhone ^ hasEmail;
    }
}
