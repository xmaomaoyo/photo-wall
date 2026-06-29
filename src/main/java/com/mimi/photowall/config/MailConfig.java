package com.mimi.photowall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置类
 * 从application.yaml中读取邮件相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
public class MailConfig {

    /**
     * 邮件服务器主机
     */
    private String host;

    /**
     * 邮件服务器端口
     */
    private Integer port;

    /**
     * 邮件用户名
     */
    private String username;

    /**
     * 邮件密码/授权码
     */
    private String password;
}
