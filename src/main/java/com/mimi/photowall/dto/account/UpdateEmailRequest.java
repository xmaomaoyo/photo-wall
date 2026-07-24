package com.mimi.photowall.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新邮箱请求
 */
@Data
@Schema(description = "更新邮箱请求")
public class UpdateEmailRequest {

    /**
     * 新邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    @Schema(description = "新邮箱", example = "user@example.com")
    private String email;
}
