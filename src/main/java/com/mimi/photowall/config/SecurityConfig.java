package com.mimi.photowall.config;

import com.mimi.photowall.common.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 配置认证规则、白名单、过滤器等
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用 @PreAuthorize 注解
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * 白名单路径 - 无需认证即可访问
     */
    private static final String[] WHITE_LIST = {
            // 认证相关
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/captcha/**",
            "/api/v1/auth/sms/send",
            "/api/v1/auth/sms/send/**",
            "/api/v1/auth/email/send",
            "/api/v1/auth/email/send/**",
            "/api/v1/auth/sms/login",
            "/api/v1/auth/email/login",
            "/api/v1/auth/password/login",
            "/api/v1/auth/password/forgot",
            "/api/v1/auth/password/reset",
            "/api/v1/auth/refresh",

            // 静态资源
            "/favicon.ico",
            "/static/**",
            "/assets/**",
            "/uploads/**",

            // API 文档
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（API 项目不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // 配置会话管理为无状态（JWT 模式）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 白名单放行
                        .requestMatchers(WHITE_LIST).permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )

                // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
