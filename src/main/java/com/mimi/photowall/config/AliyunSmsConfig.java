package com.mimi.photowall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信配置类
 * 从application.yaml中读取阿里云短信相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsConfig {

    /**
     * 阿里云AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板ID
     */
    private String templateCode;

    /**
     * 验证码有效期（秒）
     */
    private Integer expire = 300;

    /**
     * 最大验证次数
     */
    private Integer maxAttempts = 5;

    /**
     * 每日发送限制
     */
    private Integer dailyLimit = 10;
}
