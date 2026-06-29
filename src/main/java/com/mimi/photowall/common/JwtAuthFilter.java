package com.mimi.photowall.common;

import com.mimi.photowall.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器
 * 解析请求中的 JWT Token，验证有效性，并将用户信息存入 SecurityContextHolder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Authorization 请求头前缀
     */
    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 从请求头提取 Token
            String token = extractToken(request);

            // 2. 如果 Token 存在且有效，解析用户信息
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 从 Token 解析用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);
                String deviceId = jwtUtil.getDeviceIdFromToken(token);

                // 创建用户信息对象
                UserPrincipal userPrincipal = new UserPrincipal();
                userPrincipal.setUserId(userId);
                userPrincipal.setUsername(username);
                userPrincipal.setRoles(roles);
                userPrincipal.setDeviceId(deviceId);

                // 创建 Authentication 对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,  // Principal（用户信息）
                                null,           // Credentials（凭证）
                                userPrincipal.getAuthorities()  // Authorities（权限）
                        );

                // 存入 SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);

                if (log.isDebugEnabled()) {
                    log.debug("JWT认证成功: userId={}, username={}", userId, username);
                }
            }
        } catch (JwtException e) {
            log.warn("JWT Token无效: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token参数错误: {}", e.getMessage());
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     *
     * @param request HTTP 请求
     * @return Token
     */
    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith(AUTHORIZATION_PREFIX)) {
            return authorization.substring(AUTHORIZATION_PREFIX.length());
        }
        return null;
    }
}
