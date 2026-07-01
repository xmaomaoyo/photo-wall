package com.mimi.photowall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Web配置类
 * 配置CORS跨域资源共享
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PhotoUploadProperties photoUploadProperties;

    /**
     * 配置CORS
     * 允许前端跨域访问API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://192.168.31.100:5173", "http://localhost:5173")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置上传文件静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPattern = normalizeUrlPrefix(photoUploadProperties.getUrlPrefix()) + "/**";
        String location = "file:" + normalizeBasePath(photoUploadProperties.getBasePath());
        registry.addResourceHandler(urlPattern)
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        if (urlPrefix == null || urlPrefix.isBlank()) {
            return "/uploads";
        }
        return urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
    }

    private String normalizeBasePath(String basePath) {
        String normalized = basePath.replace("\\", "/");
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
