package com.mimi.photowall.service.impl;

import com.mimi.photowall.config.JwtConfig;
import com.mimi.photowall.service.TokenService;
import com.mimi.photowall.util.JwtUtil;
import com.mimi.photowall.util.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String USER_TOKENS_PREFIX = "auth:user:tokens:";

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final StringRedisTemplate redisTemplate;

    @Override
    public String[] generateTokenPair(Long userId, String username, List<String> roles,
                                      String deviceId, String ip, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(userId, username, roles, deviceId);
        String refreshToken = UUID.randomUUID().toString();
        String refreshTokenHash = hashRefreshToken(refreshToken);
        String ipValue = ip == null ? "" : ip;
        String userAgentValue = userAgent == null ? "" : userAgent;

        String refreshKey = buildRefreshKeyByHash(refreshTokenHash);
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", String.valueOf(userId));
        tokenInfo.put("username", username);
        tokenInfo.put("deviceId", deviceId);
        tokenInfo.put("deviceName", UserAgentUtil.parseDeviceName(userAgentValue));
        tokenInfo.put("ip", ipValue);
        tokenInfo.put("userAgent", userAgentValue);
        tokenInfo.put("createdAt", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(refreshKey, tokenInfo);
        redisTemplate.expire(refreshKey, jwtConfig.getRefreshTokenExpiration(), TimeUnit.SECONDS);

        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForSet().add(userTokensKey, refreshTokenHash);
        redisTemplate.expire(userTokensKey, jwtConfig.getRefreshTokenExpiration(), TimeUnit.SECONDS);

        return new String[]{accessToken, refreshToken};
    }

    @Override
    public String[] refreshTokenPair(String refreshToken, String ip, String userAgent) {
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh Token无效或已过期");
        }

        String refreshKey = buildRefreshKey(refreshToken);
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
        validateDeviceBinding(tokenInfo, userAgent);

        Long userId = Long.parseLong(tokenInfo.get("userId").toString());
        String username = tokenInfo.get("username").toString();
        String deviceId = tokenInfo.get("deviceId").toString();

        List<String> roles = new ArrayList<>();
        roles.add("USER");

        revokeRefreshToken(refreshToken);
        return generateTokenPair(userId, username, roles, deviceId, ip, userAgent);
    }

    @Override
    public boolean validateAccessToken(String accessToken) {
        return jwtUtil.validateToken(accessToken);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        return redisTemplate.hasKey(buildRefreshKey(refreshToken));
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String refreshTokenHash = hashRefreshToken(refreshToken);
        String refreshKey = buildRefreshKeyByHash(refreshTokenHash);
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
        if (!tokenInfo.isEmpty()) {
            Object userId = tokenInfo.get("userId");
            if (userId != null) {
                redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, refreshTokenHash);
            }
        }
        redisTemplate.delete(refreshKey);
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null) {
            for (String token : tokens) {
                redisTemplate.delete(buildRefreshKeyByHash(token));
            }
        }
        redisTemplate.delete(userTokensKey);
        jwtUtil.increaseTokenVersion(userId);
    }

    @Override
    public List<Map<String, Object>> getUserDevices(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);

        List<Map<String, Object>> devices = new ArrayList<>();
        if (tokens != null) {
            for (String token : tokens) {
                Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(buildRefreshKeyByHash(token));
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
                String refreshKey = buildRefreshKeyByHash(token);
                Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(refreshKey);
                if (deviceId.equals(tokenInfo.get("deviceId"))) {
                    redisTemplate.delete(refreshKey);
                    redisTemplate.opsForSet().remove(userTokensKey, token);
                    break;
                }
            }
        }
    }

    @Override
    public Long getUserIdFromRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        Map<Object, Object> tokenInfo = redisTemplate.opsForHash().entries(buildRefreshKey(refreshToken));
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

    private void validateDeviceBinding(Map<Object, Object> tokenInfo, String userAgent) {
        Object storedUserAgent = tokenInfo.get("userAgent");
        String userAgentValue = userAgent == null ? "" : userAgent;
        if (!Objects.equals(storedUserAgent, userAgentValue)) {
            throw new RuntimeException("Refresh Token设备校验失败");
        }
    }

    private String buildRefreshKey(String refreshToken) {
        return buildRefreshKeyByHash(hashRefreshToken(refreshToken));
    }

    private String buildRefreshKeyByHash(String refreshTokenHash) {
        return REFRESH_TOKEN_PREFIX + refreshTokenHash;
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256算法不可用", e);
        }
    }
}
