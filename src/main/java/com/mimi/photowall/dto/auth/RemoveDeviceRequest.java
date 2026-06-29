package com.mimi.photowall.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 踢出设备请求
 */
@Data
@Schema(description = "踢出设备请求")
public class RemoveDeviceRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID")
    private String deviceId;
}
