package com.mimi.photowall.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图形验证码信息")
public class CaptchaVO {

    /**
     * 验证码ID
     */
    @Schema(description = "验证码ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String captchaId;

    /**
     * 验证码图片（Base64）
     */
    @Schema(description = "验证码图片（Base64编码）")
    private String captchaImage;
}
