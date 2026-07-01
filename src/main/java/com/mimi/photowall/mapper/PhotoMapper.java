package com.mimi.photowall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mimi.photowall.entity.Photo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 照片Mapper接口
 */
@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {

    List<LocalDate> selectTakenDatePage(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("offset") Long offset,
            @Param("pageSize") Long pageSize
    );

    Long countTakenDateGroups(
            @Param("userId") Long userId,
            @Param("status") Integer status
    );

    List<Photo> selectByTakenDates(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("takenDates") List<LocalDate> takenDates
    );
}
