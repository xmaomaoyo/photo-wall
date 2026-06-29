package com.mimi.photowall.util;

import com.mimi.photowall.common.UserPrincipal;
import com.mimi.photowall.enums.ResultCode;
import com.mimi.photowall.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 安全工具类
 * 用于获取当前登录用户的信息
 */
public class SecurityUtils {

    private SecurityUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前用户信息对象
     *
     * @return 用户信息
     */
    public static UserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return (UserPrincipal) principal;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        return getUserPrincipal().getUserId();
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        return getUserPrincipal().getUsername();
    }

    /**
     * 获取当前用户手机号
     *
     * @return 手机号
     */
    public static String getCurrentPhone() {
        return getUserPrincipal().getPhone();
    }

    /**
     * 获取当前用户邮箱
     *
     * @return 邮箱
     */
    public static String getCurrentEmail() {
        return getUserPrincipal().getEmail();
    }

    /**
     * 获取当前用户角色列表
     *
     * @return 角色列表
     */
    public static List<String> getCurrentRoles() {
        return getUserPrincipal().getRoles();
    }

    /**
     * 获取当前用户设备ID
     *
     * @return 设备ID
     */
    public static String getCurrentDeviceId() {
        return getUserPrincipal().getDeviceId();
    }

    /**
     * 判断当前用户是否有某个角色
     *
     * @param role 角色名
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        return getUserPrincipal().hasRole(role);
    }

    /**
     * 判断当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
