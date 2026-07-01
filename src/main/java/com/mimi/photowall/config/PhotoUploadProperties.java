package com.mimi.photowall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片上传配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "photo.upload")
public class PhotoUploadProperties {

    /**
     * 上传根目录
     */
    private String basePath = "/home/photo-wall/uploads";

    /**
     * 访问URL前缀
     */
    private String urlPrefix = "/uploads";

    /**
     * 单次最大上传数量
     */
    private Integer maxBatchCount = 20;

    /**
     * 缩略图最大边
     */
    private Integer thumbnailMaxSide = 480;

    /**
     * 允许的图片内容类型
     */
    private List<String> allowedContentTypes = new ArrayList<>(
            List.of("image/jpeg", "image/png", "image/webp", "image/gif")
    );
}
