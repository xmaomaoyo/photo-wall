package com.mimi.photowall.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备信息视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备信息")
public class DeviceVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "Chrome on Windows")
    private String deviceName;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ip;

    /**
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    /**
     * 是否当前设备
     */
    @Schema(description = "是否当前设备", example = "true")
    private Boolean current;
}
