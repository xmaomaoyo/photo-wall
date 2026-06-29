package com.mimi.photowall.service;

import com.mimi.photowall.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 定义用户的查询、创建、更新等操作
 */
public interface UserService {

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户
     */
    User getUserByPhone(String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户
     */
    User getUserByEmail(String email);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    User getUserById(Long userId);

    /**
     * 创建用户
     *
     * @param user 用户
     * @return 创建的用户
     */
    User createUser(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户
     * @return 更新的用户
     */
    User updateUser(User user);

    /**
     * 更新密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码（已加密）
     */
    void updatePassword(Long userId, String newPassword);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param ip     IP地址
     */
    void updateLastLoginInfo(Long userId, String ip);

    /**
     * 根据用户ID获取角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查手机号是否已注册
     *
     * @param phone 手机号
     * @return 是否已注册
     */
    boolean isPhoneRegistered(String phone);

    /**
     * 检查邮箱是否已注册
     *
     * @param email 邮箱
     * @return 是否已注册
     */
    boolean isEmailRegistered(String email);
}
