package com.mimi.photowall.util;

import com.mimi.photowall.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析、验证JWT Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_VERSION_PREFIX = "auth:user:token-version:";

    /**
     * 生成Access Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色列表
     * @param deviceId 设备ID
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("deviceId", deviceId);
        claims.put("tokenVersion", getCurrentTokenVersion(userId));

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration() * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中解析Claims
     *
     * @param token Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从Token中获取角色列表
     *
     * @param token Token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (List<String>) claims.get("roles") : null;
    }

    /**
     * 从Token中获取设备ID
     *
     * @param token Token
     * @return 设备ID
     */
    public String getDeviceIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("deviceId") : null;
    }

    /**
     * 验证Token是否过期
     *
     * @param token Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.getExpiration().before(new Date());
        }
        return true;
    }

    /**
     * 验证Token是否有效
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null || claims.getExpiration().before(new Date())) {
                return false;
            }
            Long userId = getUserIdFromClaims(claims);
            Long tokenVersion = getLongClaim(claims, "tokenVersion");
            return userId != null && tokenVersion != null && tokenVersion.equals(getCurrentTokenVersion(userId));
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getCurrentTokenVersion(Long userId) {
        String value = redisTemplate.opsForValue().get(TOKEN_VERSION_PREFIX + userId);
        return value == null ? 0L : Long.parseLong(value);
    }

    public void increaseTokenVersion(Long userId) {
        redisTemplate.opsForValue().increment(TOKEN_VERSION_PREFIX + userId);
    }

    private Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get("userId");
        if (userId instanceof Integer integerUserId) {
            return integerUserId.longValue();
        }
        if (userId instanceof Long longUserId) {
            return longUserId;
        }
        if (userId instanceof String stringUserId) {
            return Long.parseLong(stringUserId);
        }
        return null;
    }

    private Long getLongClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof String stringValue) {
            return Long.parseLong(stringValue);
        }
        return null;
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
