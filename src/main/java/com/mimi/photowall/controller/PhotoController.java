package com.mimi.photowall.controller;

import com.mimi.photowall.common.Result;
import com.mimi.photowall.service.PhotoService;
import com.mimi.photowall.vo.PhotoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 照片控制器（示例）
 * 演示如何使用统一鉴权和数据隔离
 */
@RestController
@RequestMapping("/api/v1/photos")
@Tag(name = "照片管理", description = "照片的增删改查接口")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    /**
     * 上传照片
     */
    @PostMapping("/upload")
    @Operation(summary = "上传照片", description = "批量上传当前用户的照片，单次最多20张")
    public Result<List<PhotoVO>> uploadPhotos(@RequestPart("files") MultipartFile[] files) {
        List<PhotoVO> photos = photoService.uploadPhotos(files);
        return Result.ok(photos);
    }

    /**
     * 获取当前用户的照片列表
     * 数据隔离：只能查看自己的照片
     */
    @GetMapping
    @Operation(summary = "获取照片列表", description = "获取当前用户的照片列表")
    public Result<List<PhotoVO>> listPhotos() {
        // Service 层会自动添加 user_id 条件
        List<PhotoVO> photos = photoService.getUserPhotos();
        return Result.ok(photos);
    }

    /**
     * 获取照片详情
     * 数据隔离：只能查看自己的照片，管理员可查看所有
     */
    @GetMapping("/{photoId}")
    @Operation(summary = "获取照片详情", description = "根据ID获取照片详情")
    public Result<PhotoVO> getPhoto(@PathVariable Long photoId) {
        // Service 层会校验数据归属
        PhotoVO photo = photoService.getPhoto(photoId);
        return Result.ok(photo);
    }

    /**
     * 删除照片
     */
    @PostMapping("/{photoId}/delete")
    @Operation(summary = "删除照片", description = "删除当前用户自己的照片")
    public Result<Void> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return Result.ok();
    }

    /**
     * 获取所有照片（仅管理员）
     * 使用 @PreAuthorize 注解进行角色校验
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有照片", description = "管理员获取所有照片")
    public Result<List<PhotoVO>> getAllPhotos() {
        // 管理员可以查看所有数据，不添加 user_id 条件
        List<PhotoVO> photos = photoService.getAllPhotos();
        return Result.ok(photos);
    }
}
