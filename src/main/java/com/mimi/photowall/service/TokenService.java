package com.mimi.photowall.service;

import java.util.List;
import java.util.Map;

/**
 * Token服务接口
 * 定义Token的生成、刷新、验证、吊销等操作
 */
public interface TokenService {

    /**
     * 生成Token对
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色列表
     * @param deviceId 设备ID
     * @param ip       IP地址
     * @param userAgent 用户代理
     * @return Token对 [accessToken, refreshToken]
     */
    String[] generateTokenPair(Long userId, String username, List<String> roles,
                               String deviceId, String ip, String userAgent);

    /**
     * 刷新Token对
     *
     * @param refreshToken Refresh Token
     * @return 新的Token对 [accessToken, refreshToken]
     */
    String[] refreshTokenPair(String refreshToken, String ip, String userAgent);

    /**
     * 验证Access Token
     *
     * @param accessToken Access Token
     * @return 是否有效
     */
    boolean validateAccessToken(String accessToken);

    /**
     * 验证Refresh Token
     *
     * @param refreshToken Refresh Token
     * @return 是否有效
     */
    boolean validateRefreshToken(String refreshToken);

    /**
     * 吊销Refresh Token
     *
     * @param refreshToken Refresh Token
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * 吊销用户的所有Refresh Token
     *
     * @param userId 用户ID
     */
    void revokeAllUserTokens(Long userId);

    /**
     * 获取用户的所有设备信息
     *
     * @param userId 用户ID
     * @return 设备信息列表
     */
    List<Map<String, Object>> getUserDevices(Long userId);

    /**
     * 踢出指定设备
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void removeDevice(Long userId, String deviceId);

    /**
     * 从Refresh Token中获取用户ID
     *
     * @param refreshToken Refresh Token
     * @return 用户ID
     */
    Long getUserIdFromRefreshToken(String refreshToken);

    /**
     * 从Access Token中获取用户ID
     *
     * @param accessToken Access Token
     * @return 用户ID
     */
    Long getUserIdFromAccessToken(String accessToken);

    /**
     * 从Access Token中获取角色列表
     *
     * @param accessToken Access Token
     * @return 角色列表
     */
    List<String> getRolesFromAccessToken(String accessToken);
}
