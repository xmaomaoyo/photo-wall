package com.mimi.photowall.controller;

import com.mimi.photowall.common.Result;
import com.mimi.photowall.service.PhotoService;
import com.mimi.photowall.vo.PageVO;
import com.mimi.photowall.vo.PhotoDateGroupVO;
import com.mimi.photowall.vo.PhotoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 照片控制器
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
     * 分页获取当前用户的全部照片
     */
    @GetMapping
    @Operation(summary = "查看全部照片", description = "分页获取当前用户的全部照片")
    public Result<PageVO<PhotoVO>> listPhotos(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        PageVO<PhotoVO> photos = photoService.getUserPhotos(pageNum, pageSize);
        return Result.ok(photos);
    }

    /**
     * 按拍摄日期分组获取当前用户照片
     */
    @GetMapping("/by-date")
    @Operation(summary = "按时间查看照片", description = "按taken_time日期分组分页获取当前用户照片")
    public Result<PageVO<PhotoDateGroupVO>> listPhotosByTakenDate(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        PageVO<PhotoDateGroupVO> photos = photoService.getUserPhotosByTakenDate(pageNum, pageSize);
        return Result.ok(photos);
    }

    /**
     * 获取照片详情
     */
    @GetMapping("/{photoId}")
    @Operation(summary = "获取照片详情", description = "根据ID获取照片详情")
    public Result<PhotoVO> getPhoto(@PathVariable Long photoId) {
        PhotoVO photo = photoService.getPhoto(photoId);
        return Result.ok(photo);
    }

    /**
     * 删除当前用户照片
     */
    @PostMapping("/{photoId}/delete")
    @Operation(summary = "删除照片", description = "删除当前用户自己的照片")
    public Result<Void> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return Result.ok();
    }

    /**
     * 管理员获取全部照片
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取全部照片", description = "管理员获取全部照片")
    public Result<List<PhotoVO>> getAllPhotos() {
        List<PhotoVO> photos = photoService.getAllPhotos();
        return Result.ok(photos);
    }
}
