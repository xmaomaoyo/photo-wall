package com.mimi.photowall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 配置类
 * 用于自定义 API 文档的标题、描述、版本、联系人等信息
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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
