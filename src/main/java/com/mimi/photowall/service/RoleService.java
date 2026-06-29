package com.mimi.photowall.service;

import com.mimi.photowall.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * 定义角色的查询、创建、更新、删除等操作
 */
public interface RoleService {

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色
     */
    Role getRoleById(Long roleId);

    /**
     * 根据角色编码获取角色
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    Role getRoleByCode(String roleCode);

    /**
     * 创建角色
     *
     * @param role 角色
     * @return 创建的角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     *
     * @param role 角色
     * @return 更新的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignUserRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoleCodes(Long userId);

    /**
     * 检查角色编码是否已存在
     *
     * @param roleCode 角色编码
     * @return 是否存在
     */
    boolean isRoleCodeExists(String roleCode);

    /**
     * 检查角色是否有用户使用
     *
     * @param roleId 角色ID
     * @return 是否有用户使用
     */
    boolean isRoleInUse(Long roleId);
}
