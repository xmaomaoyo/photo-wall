package com.mimi.photowall.service;

import com.mimi.photowall.vo.PhotoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 照片服务（示例）
 * 演示如何使用数据隔离
 */
@Slf4j
@Service
public class PhotoService extends BaseService {

    /**
     * 获取当前用户的照片列表
     * 使用 createUserQuery() 自动添加 user_id 条件
     */
    public List<PhotoVO> getUserPhotos() {
        Long userId = getCurrentUserId();
        log.debug("获取用户照片列表: userId={}", userId);

        // 实际项目中应该这样使用：
        // QueryWrapper<Photo> wrapper = createUserQuery();
        // List<Photo> photos = photoMapper.selectList(wrapper);
        // return photos.stream().map(this::toVO).collect(Collectors.toList());

        // 示例返回
        List<PhotoVO> photos = new ArrayList<>();
        PhotoVO photo = PhotoVO.builder()
                .id(1L)
                .userId(userId)
                .title("示例照片")
                .description("这是示例照片")
                .url("https://example.com/photo.jpg")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        photos.add(photo);
        return photos;
    }

    /**
     * 获取照片详情
     * 使用 checkDataOwnership() 校验数据归属
     */
    public PhotoVO getPhoto(Long photoId) {
        // 实际项目中应该这样使用：
        // Photo photo = photoMapper.selectById(photoId);
        // if (photo == null) {
        //     throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        // }
        // checkDataOwnership(photo.getUserId());  // 校验数据归属
        // return toVO(photo);

        // 示例返回
        return PhotoVO.builder()
                .id(photoId)
                .userId(getCurrentUserId())
                .title("示例照片")
                .description("这是示例照片")
                .url("https://example.com/photo.jpg")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取所有照片（仅管理员）
     * 不添加 user_id 条件
     */
    public List<PhotoVO> getAllPhotos() {
        // 管理员接口，不需要数据隔离
        // List<Photo> photos = photoMapper.selectList(null);
        // return photos.stream().map(this::toVO).collect(Collectors.toList());

        // 示例返回
        List<PhotoVO> photos = new ArrayList<>();
        PhotoVO photo = PhotoVO.builder()
                .id(1L)
                .userId(100L)
                .title("所有照片")
                .description("管理员查看所有照片")
                .url("https://example.com/photo.jpg")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        photos.add(photo);
        return photos;
    }
}
