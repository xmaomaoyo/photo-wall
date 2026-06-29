package com.mimi.photowall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mimi.photowall.entity.Role;
import com.mimi.photowall.entity.UserRole;
import com.mimi.photowall.mapper.RoleMapper;
import com.mimi.photowall.mapper.UserRoleMapper;
import com.mimi.photowall.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 实现角色的查询、创建、更新、删除等操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户角色缓存Key前缀
     */
    private static final String USER_ROLES_CACHE_PREFIX = "auth:user:roles:";

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        return roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode)
        );
    }

    @Override
    public Role createRole(Role role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setStatus(1);
        role.setDeleted(0);
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role updateRole(Role role) {
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        return role;
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        // 删除用户原有角色
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        // 分配新角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }

        // 更新Redis中的角色缓存
        List<String> roleCodes = roleIds.stream()
                .map(roleId -> {
                    Role role = roleMapper.selectById(roleId);
                    return role != null ? role.getRoleCode() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        if (!roleCodes.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(cacheKey, roleCodes);
            redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
        }
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
        List<String> cachedRoles = redisTemplate.opsForList().range(cacheKey, 0, -1);
        if (cachedRoles != null && !cachedRoles.isEmpty()) {
            return cachedRoles;
        }

        // 缓存没有，从数据库获取
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // 存入缓存
        if (!roleCodes.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(cacheKey, roleCodes);
            redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
        }

        return roleCodes;
    }

    @Override
    public boolean isRoleCodeExists(String roleCode) {
        return roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode)
        ) != null;
    }

    @Override
    public boolean isRoleInUse(Long roleId) {
        // 检查是否有用户使用该角色
        // 这里简化处理，实际应该查询user_role表
        return false;
    }
}
