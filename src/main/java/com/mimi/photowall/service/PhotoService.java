package com.mimi.photowall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.mimi.photowall.config.PhotoUploadProperties;
import com.mimi.photowall.entity.Photo;
import com.mimi.photowall.enums.ResultCode;
import com.mimi.photowall.exception.BusinessException;
import com.mimi.photowall.mapper.PhotoMapper;
import com.mimi.photowall.vo.PhotoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 照片服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService extends BaseService {

    private static final int NORMAL_STATUS = 1;

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final Set<String> JPEG_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg");

    private final PhotoMapper photoMapper;

    private final PhotoUploadProperties photoUploadProperties;

    /**
     * 批量上传照片
     *
     * @param files 上传文件数组
     * @return 照片信息列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<PhotoVO> uploadPhotos(MultipartFile[] files) {
        validateBatch(files);
        List<Path> createdPaths = new ArrayList<>();
        List<Photo> savedPhotos = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                Photo photo = saveOnePhoto(file, createdPaths);
                photoMapper.insert(photo);
                savedPhotos.add(photo);
            }
            return savedPhotos.stream().map(this::toVO).toList();
        } catch (RuntimeException e) {
            cleanupCreatedFiles(createdPaths);
            throw e;
        }
    }

    /**
     * 获取当前用户的照片列表
     */
    public List<PhotoVO> getUserPhotos() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(Photo::getUserId, userId)
                .eq(Photo::getStatus, NORMAL_STATUS)
                .orderByDesc(Photo::getTakenTime)
                .orderByDesc(Photo::getUploadTime);
        return photoMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    /**
     * 获取照片详情
     *
     * @param photoId 照片ID
     * @return 照片信息
     */
    public PhotoVO getPhoto(Long photoId) {
        Photo photo = getExistingPhoto(photoId);
        checkDataOwnership(photo.getUserId(), "无权访问该照片");
        return toVO(photo);
    }

    /**
     * 删除当前用户照片
     *
     * @param photoId 照片ID
     */
    public void deletePhoto(Long photoId) {
        Photo photo = getExistingPhoto(photoId);
        checkDataOwnership(photo.getUserId(), "无权删除该照片");
        photoMapper.deleteById(photoId);
        cleanupPhotoFiles(photo);
    }

    /**
     * 获取所有照片（仅管理员）
     */
    public List<PhotoVO> getAllPhotos() {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<Photo>()
                .eq(Photo::getStatus, NORMAL_STATUS)
                .orderByDesc(Photo::getUploadTime);
        return photoMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    private Photo saveOnePhoto(MultipartFile file, List<Path> createdPaths) {
        validateFile(file);
        LocalDateTime uploadTime = LocalDateTime.now();
        String contentType = normalizeContentType(file.getContentType());
        String extension = resolveExtension(contentType, file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "." + extension;
        String datePath = DATE_PATH_FORMATTER.format(LocalDate.now());

        Path originalPath = buildStoragePath("originals", datePath, storedFilename);
        Path thumbnailPath = buildStoragePath("thumbnails", datePath, storedFilename);
        saveOriginal(file, originalPath, createdPaths);

        ImageInfo imageInfo = readImageInfo(originalPath);
        generateThumbnail(originalPath, thumbnailPath, createdPaths);
        LocalDateTime takenTime = readTakenTime(originalPath, uploadTime);

        Photo photo = new Photo();
        photo.setUserId(getCurrentUserId());
        photo.setOriginalFilename(safeOriginalFilename(file.getOriginalFilename()));
        photo.setStoredFilename(storedFilename);
        photo.setStoragePath(originalPath.toString());
        photo.setUrl(buildUrl("originals", datePath, storedFilename));
        photo.setThumbnailPath(thumbnailPath.toString());
        photo.setThumbnailUrl(buildUrl("thumbnails", datePath, storedFilename));
        photo.setContentType(contentType);
        photo.setFileSize(file.getSize());
        photo.setWidth(imageInfo.width());
        photo.setHeight(imageInfo.height());
        photo.setTakenTime(takenTime);
        photo.setUploadTime(uploadTime);
        photo.setStatus(NORMAL_STATUS);
        photo.setDeleted(0);
        return photo;
    }

    private void validateBatch(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }
        Integer maxBatchCount = photoUploadProperties.getMaxBatchCount();
        if (files.length > maxBatchCount) {
            throw new BusinessException(
                    ResultCode.FILE_BATCH_COUNT_EXCEEDED,
                    "单次最多上传" + maxBatchCount + "张图片"
            );
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!photoUploadProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private void saveOriginal(MultipartFile file, Path originalPath, List<Path> createdPaths) {
        try {
            Files.createDirectories(originalPath.getParent());
            file.transferTo(originalPath);
            createdPaths.add(originalPath);
        } catch (IOException e) {
            log.error("保存原图失败: path={}", originalPath, e);
            throw new BusinessException(ResultCode.FILE_STORAGE_FAILED);
        }
    }

    private ImageInfo readImageInfo(Path originalPath) {
        try {
            BufferedImage image = ImageIO.read(originalPath.toFile());
            if (image == null) {
                throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "图片内容不可读取");
            }
            return new ImageInfo(image.getWidth(), image.getHeight());
        } catch (IOException e) {
            log.warn("读取图片尺寸失败: path={}", originalPath, e);
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "图片内容不可读取");
        }
    }

    private void generateThumbnail(Path originalPath, Path thumbnailPath, List<Path> createdPaths) {
        try {
            Files.createDirectories(thumbnailPath.getParent());
            Thumbnails.of(originalPath.toFile())
                    .size(photoUploadProperties.getThumbnailMaxSide(), photoUploadProperties.getThumbnailMaxSide())
                    .keepAspectRatio(true)
                    .toFile(thumbnailPath.toFile());
            createdPaths.add(thumbnailPath);
        } catch (IOException e) {
            log.error("生成缩略图失败: original={}, thumbnail={}", originalPath, thumbnailPath, e);
            throw new BusinessException(ResultCode.FILE_STORAGE_FAILED, "生成缩略图失败");
        }
    }

    private LocalDateTime readTakenTime(Path originalPath, LocalDateTime uploadTime) {
        try (InputStream inputStream = Files.newInputStream(originalPath)) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            Date date = firstDate(metadata);
            if (date == null) {
                return uploadTime;
            }
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } catch (Exception e) {
            log.debug("读取图片时间失败，使用上传时间: path={}", originalPath, e);
            return uploadTime;
        }
    }

    private Date firstDate(Metadata metadata) {
        Date originalDate = getDate(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        if (originalDate != null) {
            return originalDate;
        }
        Date digitizedDate = getDate(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
        if (digitizedDate != null) {
            return digitizedDate;
        }
        return getDate(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_DATETIME);
    }

    private <T extends Directory> Date getDate(Metadata metadata, Class<T> directoryType, int tagType) {
        T directory = metadata.getFirstDirectoryOfType(directoryType);
        if (directory == null) {
            return null;
        }
        return directory.getDate(tagType);
    }

    private Photo getExistingPhoto(Long photoId) {
        Photo photo = photoMapper.selectById(photoId);
        if (photo == null) {
            throw new BusinessException(ResultCode.PHOTO_NOT_FOUND);
        }
        return photo;
    }

    private void cleanupCreatedFiles(List<Path> createdPaths) {
        for (Path path : createdPaths) {
            deleteFileQuietly(path);
        }
    }

    private void cleanupPhotoFiles(Photo photo) {
        deleteFileQuietly(Path.of(photo.getStoragePath()));
        deleteFileQuietly(Path.of(photo.getThumbnailPath()));
    }

    private void deleteFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除照片文件失败: path={}", path, e);
        }
    }

    private Path buildStoragePath(String type, String datePath, String storedFilename) {
        return Path.of(photoUploadProperties.getBasePath(), type, datePath, storedFilename);
    }

    private String buildUrl(String type, String datePath, String storedFilename) {
        String urlPrefix = normalizeUrlPrefix(photoUploadProperties.getUrlPrefix());
        return urlPrefix + "/" + type + "/" + datePath + "/" + storedFilename;
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        if (urlPrefix == null || urlPrefix.isBlank()) {
            return "/uploads";
        }
        String normalized = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private String resolveExtension(String contentType, String originalFilename) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> {
                String filename = safeOriginalFilename(originalFilename);
                int dotIndex = filename.lastIndexOf('.');
                if (JPEG_CONTENT_TYPES.contains(contentType) && dotIndex > -1) {
                    String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
                    if ("jpeg".equals(extension)) {
                        yield "jpeg";
                    }
                }
                yield "jpg";
            }
        };
    }

    private String safeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unknown";
        }
        String normalized = originalFilename.replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex > -1) {
            return normalized.substring(slashIndex + 1);
        }
        return normalized;
    }

    private PhotoVO toVO(Photo photo) {
        return PhotoVO.builder()
                .id(photo.getId())
                .userId(photo.getUserId())
                .title(photo.getOriginalFilename())
                .description(null)
                .url(photo.getUrl())
                .thumbnailUrl(photo.getThumbnailUrl())
                .originalFilename(photo.getOriginalFilename())
                .contentType(photo.getContentType())
                .fileSize(photo.getFileSize())
                .width(photo.getWidth())
                .height(photo.getHeight())
                .takenTime(photo.getTakenTime())
                .uploadTime(photo.getUploadTime())
                .status(photo.getStatus())
                .createTime(photo.getCreateTime())
                .updateTime(photo.getUpdateTime())
                .build();
    }

    private record ImageInfo(Integer width, Integer height) {
    }
}
