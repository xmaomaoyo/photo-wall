package com.mimi.photowall.common;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息上下文
 * 实现 Spring Security 的 UserDetails 接口，存储当前登录用户的信息
 */
@Data
public class UserPrincipal implements UserDetails {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 获取权限列表（Spring Security 需要）
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * 获取密码（JWT 模式不需要，返回 null）
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * 获取用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 判断是否有某个角色
     *
     * @param role 角色名
     * @return 是否有该角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
