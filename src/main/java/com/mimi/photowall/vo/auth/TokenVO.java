package com.mimi.photowall.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token信息")
public class TokenVO {

    /**
     * Access Token
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * Refresh Token
     */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /**
     * Access Token过期时间（秒）
     */
    @Schema(description = "访问令牌过期时间（秒）", example = "1800")
    private Long expiresIn;
}
