package com.mimi.photowall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mimi.photowall.util.SecurityUtils;

/**
 * 基础服务类
 * 提供数据隔离的通用方法，所有 Service 应继承此类
 */
public abstract class BaseService {

    /**
     * 创建带用户隔离的查询条件
     * 自动添加 user_id = 当前用户ID 的条件
     *
     * @return 带用户隔离的 QueryWrapper
     */
    protected <T> QueryWrapper<T> createUserQuery() {
        return new QueryWrapper<T>().eq("user_id", SecurityUtils.getCurrentUserId());
    }

    /**
     * 创建带用户隔离的查询条件
     * 自动添加 user_id = 当前用户ID 的条件，并支持额外条件
     *
     * @param wrapper 额外的查询条件
     * @return 带用户隔离的 QueryWrapper
     */
    protected <T> QueryWrapper<T> createUserQuery(QueryWrapper<T> wrapper) {
        if (wrapper == null) {
            return createUserQuery();
        }
        return wrapper.eq("user_id", SecurityUtils.getCurrentUserId());
    }

    /**
     * 校验数据归属
     * 检查数据是否属于当前用户，管理员跳过检查
     *
     * @param dataUserId 数据所属的用户ID
     * @param message    错误提示信息
     */
    protected void checkDataOwnership(Long dataUserId, String message) {
        // 管理员可以访问所有数据
        if (SecurityUtils.isAdmin()) {
            return;
        }

        // 普通用户只能访问自己的数据
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(dataUserId)) {
            throw new RuntimeException(message);
        }
    }

    /**
     * 校验数据归属（默认错误信息）
     *
     * @param dataUserId 数据所属的用户ID
     */
    protected void checkDataOwnership(Long dataUserId) {
        checkDataOwnership(dataUserId, "无权访问该数据");
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    protected Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * 判断当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    protected boolean isAdmin() {
        return SecurityUtils.isAdmin();
    }
}
