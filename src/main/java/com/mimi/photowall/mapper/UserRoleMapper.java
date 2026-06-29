package com.mimi.photowall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimi.photowall.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 * 继承BaseMapper，提供基础CRUD操作
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
