package com.mimi.photowall.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新手机号请求
 */
@Data
@Schema(description = "更新手机号请求")
public class UpdatePhoneRequest {

    /**
     * 新手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "新手机号", example = "13800138000")
    private String phone;
}
