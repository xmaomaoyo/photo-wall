package com.mimi.photowall.vo.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号资料视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号资料")
public class AccountProfileVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "脱敏手机号")
    private String phone;

    @Schema(description = "是否已绑定手机号")
    private Boolean phoneBound;

    @Schema(description = "脱敏邮箱")
    private String email;

    @Schema(description = "是否已绑定邮箱")
    private Boolean emailBound;
}
