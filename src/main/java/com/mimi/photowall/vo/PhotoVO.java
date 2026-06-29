package com.mimi.photowall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 照片视图对象
 * 用于返回给前端的照片信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "照片信息")
public class PhotoVO {

    /**
     * 照片ID
     */
    @Schema(description = "照片ID", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "100")
    private Long userId;

    /**
     * 照片标题
     */
    @Schema(description = "照片标题", example = "风景照片")
    private String title;

    /**
     * 照片描述
     */
    @Schema(description = "照片描述", example = "美丽的风景")
    private String description;

    /**
     * 照片URL
     */
    @Schema(description = "照片URL", example = "https://example.com/photo.jpg")
    private String url;

    /**
     * 缩略图URL
     */
    @Schema(description = "缩略图URL", example = "https://example.com/thumb.jpg")
    private String thumbnailUrl;

    /**
     * 照片状态
     */
    @Schema(description = "照片状态", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
