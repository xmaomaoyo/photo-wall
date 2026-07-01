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
     * 原始文件名
     */
    @Schema(description = "原始文件名", example = "photo.jpg")
    private String originalFilename;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型", example = "image/jpeg")
    private String contentType;

    /**
     * 文件大小，单位字节
     */
    @Schema(description = "文件大小，单位字节", example = "102400")
    private Long fileSize;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度", example = "1920")
    private Integer width;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度", example = "1080")
    private Integer height;

    /**
     * 图片时间
     */
    @Schema(description = "图片时间")
    private LocalDateTime takenTime;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

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
