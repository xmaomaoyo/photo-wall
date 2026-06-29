package com.mimi.photowall.service.impl;

import com.mimi.photowall.config.JwtConfig;
import com.mimi.photowall.service.TokenService;
import com.mimi.photowall.util.JwtUtil;
import com.mimi.photowall.util.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Token服务实现类
 * 实现Token的生成、刷新、验证、吊销等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final StringRedisTemplate redisTemplate;

    /**
     * Refresh Token存储Key前缀
     */
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    /**
     * 用户Token集合Key前缀
     */
    private static final String USER_TOKENS_PREFIX = "auth:user:tokens:";

    @Override
    public String[] generateTokenPair(Long userId, String username, List<String> roles,
                                      String deviceId, String ip, String userAgent) {
        // 生成Access Token
        String accessToken = jwtUtil.generateAccessToken(userId, username, roles, deviceId);

        // 生成Refresh Token
        String refreshToken = UUID.randomUUID().toString();

        // 存储Refresh Token到Redis
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", String.valueOf(userId));
        tokenInfo.put("username", username);
        tokenInfo.put("deviceId", deviceId);
        tokenInfo.put("deviceName", UserAgentUtil.parseDeviceName(userAgent));
        tokenInfo.put("ip", ip);
        tokenInfo.put("userAgent", userAgent);
        tokenInfo.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(refreshKey, tokenInfo);
        redisTemplate.expire(refreshKey, jwtConfig.getRefreshTokenExpiration(), TimeUnit.SECONDS);

        // 添加到用户的Token集合
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForSet().add(userTokensKey, refreshToken);
        redisTemplate.expire(userTokensKey, jwtConfig.getRefreshTokenExpiration(), TimeUnit.SECONDS);

        return new String[]{accessToken, refreshToken};
    }

    @Override
    public String[] refreshTokenPair(String refreshToken) {
        // 验证Refresh Token
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh Token无效或已过期");
        }

        // 获取用户信息
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);

        Long userId = Long.parseLong(tokenInfo.get("userId").toString());
        String username = tokenInfo.get("username").toString();
        String deviceId = tokenInfo.get("deviceId").toString();
        String ip = tokenInfo.get("ip").toString();
        String userAgent = tokenInfo.get("userAgent").toString();

        // 获取用户角色
        // 这里简化处理，实际应该从数据库或缓存获取
        List<String> roles = new ArrayList<>();
        roles.add("USER");

        // 吊销旧的Refresh Token
        revokeRefreshToken(refreshToken);

        // 生成新的Token对
        return generateTokenPair(userId, username, roles, deviceId, ip, userAgent);
    }

    @Override
    public boolean validateAccessToken(String accessToken) {
        return jwtUtil.validateToken(accessToken);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        return redisTemplate.hasKey(refreshKey);
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(refreshKey);
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null) {
            for (String token : tokens) {
                String refreshKey = REFRESH_TOKEN_PREFIX + token;
                redisTemplate.delete(refreshKey);
            }
        }
        redisTemplate.delete(userTokensKey);
    }

    @Override
    public List<Map<String, Object>> getUserDevices(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);

        List<Map<String, Object>> devices = new ArrayList<>();
        if (tokens != null) {
            for (String token : tokens) {
                String refreshKey = REFRESH_TOKEN_PREFIX + token;
                Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
                if (!tokenInfo.isEmpty()) {
                    Map<String, Object> device = new HashMap<>();
                    device.put("id", tokenInfo.get("deviceId"));
                    device.put("deviceName", tokenInfo.get("deviceName"));
                    device.put("ip", tokenInfo.get("ip"));
                    device.put("lastActiveTime", tokenInfo.get("createdAt"));
                    devices.add(device);
                }
            }
        }
        return devices;
    }

    @Override
    public void removeDevice(Long userId, String deviceId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null) {
            for (String token : tokens) {
                String refreshKey = REFRESH_TOKEN_PREFIX + token;
                Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
                if (deviceId.equals(tokenInfo.get("deviceId"))) {
                    revokeRefreshToken(token);
                    redisTemplate.opsForSet().remove(userTokensKey, token);
                    break;
                }
            }
        }
    }

    @Override
    public Long getUserIdFromRefreshToken(String refreshToken) {
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
        if (tokenInfo.isEmpty()) {
            return null;
        }
        return Long.parseLong(tokenInfo.get("userId").toString());
    }

    @Override
    public Long getUserIdFromAccessToken(String accessToken) {
        return jwtUtil.getUserIdFromToken(accessToken);
    }

    @Override
    public List<String> getRolesFromAccessToken(String accessToken) {
        return jwtUtil.getRolesFromToken(accessToken);
    }
}
