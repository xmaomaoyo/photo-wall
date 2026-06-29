package com.mimi.photowall.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应信息")
public class LoginVO {

    /**
     * Token信息
     */
    @Schema(description = "Token信息")
    private TokenVO token;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;

    /**
     * 是否需要重置密码（自动注册时为true）
     */
    @Schema(description = "是否需要重置密码", example = "false")
    private Boolean needResetPassword;
}
