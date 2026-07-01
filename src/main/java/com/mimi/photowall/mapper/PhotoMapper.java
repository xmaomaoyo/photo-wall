package com.mimi.photowall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimi.photowall.entity.Photo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 照片Mapper接口
 */
@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {
}
