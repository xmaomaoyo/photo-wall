package com.mimi.photowall.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料请求
 */
@Data
@Schema(description = "更新用户资料请求")
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    @Schema(description = "昵称", example = "小明")
    private String nickname;
}
