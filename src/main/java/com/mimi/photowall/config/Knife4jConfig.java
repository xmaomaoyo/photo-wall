package com.mimi.photowall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 配置类
 * 用于自定义 API 文档的标题、描述、版本、联系人等信息
 * 配置 JWT Bearer Token 认证方案
 */
@Configuration
public class Knife4jConfig {

    /**
     * 安全方案名称
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Token";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 添加全局安全要求
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // 配置安全方案
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Token 认证，请输入 Bearer Token")))
                // 配置 API 信息
                .info(new Info()
                        .title("photo-wall 接口文档")
                        .description("photo-wall 项目 API 接口文档，基于 OpenAPI3 + Knife4j")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("mimi")
                                .email("mimi@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
