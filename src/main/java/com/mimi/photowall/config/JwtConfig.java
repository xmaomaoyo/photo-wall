package com.mimi.photowall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 从application.yaml中读取JWT相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * Access Token过期时间（秒）
     */
    private Long accessTokenExpiration = 1800L;

    /**
     * Refresh Token过期时间（秒）
     */
    private Long refreshTokenExpiration = 604800L;
}
