package com.mimi.photowall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * 应用启动完成监听器
 * 在 Spring Boot 启动完成后打印启动信息
 */
@Slf4j
@Component
public class StartupCompleteListener {

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.profiles.active:default}")
    private String profile;

    private final Environment environment;

    public StartupCompleteListener(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String localUrl = String.format("http://localhost:%s%s", port, contextPath);
            String networkUrl = String.format("http://%s:%s%s", host, port, contextPath);
            String docUrl = String.format("http://localhost:%s%s/doc.html", port, contextPath);

            System.out.println("\n" +
                    "╔══════════════════════════════════════════════════════════════╗\n" +
                    "║                                                              ║\n" +
                    "║              照片墙后端服务启动成功！                             ║\n" +
                    "║                                                              ║\n" +
                    "╠══════════════════════════════════════════════════════════════╣\n" +
                    "║  本地访问地址: " + String.format("%-46s", localUrl) + "  ║\n" +
                    "║  网络访问地址: " + String.format("%-46s", networkUrl) + "║\n" +
                    "║  激活的环境:   " + String.format("%-46s", profile) + "║\n" +
                    "║  API文档地址:  " + String.format("%-46s", docUrl) + "║\n" +
                    "║                                                              ║\n" +
                    "╚══════════════════════════════════════════════════════════════╝");

            log.info("照片墙后端服务启动完成 - 端口: {}, 环境: {}", port, profile);
        } catch (Exception e) {
            log.error("获取启动信息失败", e);
        }
    }
}
