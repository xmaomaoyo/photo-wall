package com.mimi.photowall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimi.photowall.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper接口
 * 继承BaseMapper，提供基础CRUD操作
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
