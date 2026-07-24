package com.mimi.photowall.service.impl;

import com.mimi.photowall.config.PhotoUploadProperties;
import com.mimi.photowall.dto.account.UpdateEmailRequest;
import com.mimi.photowall.dto.account.UpdatePhoneRequest;
import com.mimi.photowall.dto.account.UpdateProfileRequest;
import com.mimi.photowall.entity.User;
import com.mimi.photowall.enums.ResultCode;
import com.mimi.photowall.exception.BusinessException;
import com.mimi.photowall.service.AccountService;
import com.mimi.photowall.service.TokenService;
import com.mimi.photowall.service.UserService;
import com.mimi.photowall.util.DesensitizeUtil;
import com.mimi.photowall.util.SecurityUtils;
import com.mimi.photowall.vo.account.AccountProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 账号管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int ACTIVE_STATUS = 1;

    private static final int DISABLED_STATUS = 0;

    private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;

    private static final int AVATAR_MAX_SIDE = 512;

    private static final String AVATAR_DIRECTORY = "avatars";

    private static final Set<String> AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    private final UserService userService;

    private final TokenService tokenService;

    private final PhotoUploadProperties photoUploadProperties;

    @Override
    public AccountProfileVO getProfile() {
        return toProfileVO(getCurrentUser());
    }

    @Override
    public AccountProfileVO updateProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        User changedUser = new User();
        changedUser.setId(currentUser.getId());
        changedUser.setNickname(request.getNickname().trim());
        userService.updateUser(changedUser);
        currentUser.setNickname(changedUser.getNickname());
        return toProfileVO(currentUser);
    }

    @Override
    public AccountProfileVO updateAvatar(MultipartFile file) {
        validateAvatar(file);
        User currentUser = getCurrentUser();
        String contentType = normalizeContentType(file.getContentType());
        String storedFilename = UUID.randomUUID() + resolveExtension(contentType);
        Path avatarPath = buildAvatarPath(currentUser.getId(), storedFilename);

        saveAvatar(file, avatarPath);
        String avatarUrl = buildAvatarUrl(currentUser.getId(), storedFilename);
        try {
            User changedUser = new User();
            changedUser.setId(currentUser.getId());
            changedUser.setAvatar(avatarUrl);
            userService.updateUser(changedUser);
        } catch (RuntimeException exception) {
            deleteFileQuietly(avatarPath);
            throw exception;
        }

        deletePreviousAvatar(currentUser.getAvatar(), avatarPath);
        currentUser.setAvatar(avatarUrl);
        return toProfileVO(currentUser);
    }

    @Override
    public AccountProfileVO updatePhone(UpdatePhoneRequest request) {
        User currentUser = getCurrentUser();
        String phone = request.getPhone().trim();
        User registeredUser = userService.getUserByPhone(phone);
        if (registeredUser != null && !currentUser.getId().equals(registeredUser.getId())) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
        }

        User changedUser = new User();
        changedUser.setId(currentUser.getId());
        changedUser.setPhone(phone);
        try {
            userService.updateUser(changedUser);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_REGISTERED);
        }
        currentUser.setPhone(phone);
        return toProfileVO(currentUser);
    }

    @Override
    public AccountProfileVO updateEmail(UpdateEmailRequest request) {
        User currentUser = getCurrentUser();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User registeredUser = userService.getUserByEmail(email);
        if (registeredUser != null && !currentUser.getId().equals(registeredUser.getId())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_REGISTERED);
        }

        User changedUser = new User();
        changedUser.setId(currentUser.getId());
        changedUser.setEmail(email);
        try {
            userService.updateUser(changedUser);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_REGISTERED);
        }
        currentUser.setEmail(email);
        return toProfileVO(currentUser);
    }

    @Override
    public void disableAccount() {
        User currentUser = getCurrentUser();
        if (!Integer.valueOf(ACTIVE_STATUS).equals(currentUser.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        User changedUser = new User();
        changedUser.setId(currentUser.getId());
        changedUser.setStatus(DISABLED_STATUS);
        userService.updateUser(changedUser);
        tokenService.revokeAllUserTokens(currentUser.getId());
    }

    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private AccountProfileVO toProfileVO(User user) {
        return AccountProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(DesensitizeUtil.desensitizePhone(user.getPhone()))
                .phoneBound(StringUtils.hasText(user.getPhone()))
                .email(DesensitizeUtil.desensitizeEmail(user.getEmail()))
                .emailBound(StringUtils.hasText(user.getEmail()))
                .build();
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "头像大小不能超过5MB");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!AVATAR_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private void saveAvatar(MultipartFile file, Path avatarPath) {
        try {
            Files.createDirectories(avatarPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Thumbnails.of(inputStream)
                        .size(AVATAR_MAX_SIDE, AVATAR_MAX_SIDE)
                        .keepAspectRatio(true)
                        .toFile(avatarPath.toFile());
            }
        } catch (IOException exception) {
            deleteFileQuietly(avatarPath);
            log.warn("保存头像失败: path={}", avatarPath, exception);
            throw new BusinessException(ResultCode.FILE_STORAGE_FAILED, "头像保存失败");
        }
    }

    private Path buildAvatarPath(Long userId, String storedFilename) {
        return getUploadRoot()
                .resolve(AVATAR_DIRECTORY)
                .resolve(String.valueOf(userId))
                .resolve(storedFilename)
                .normalize();
    }

    private String buildAvatarUrl(Long userId, String storedFilename) {
        return normalizeUrlPrefix(photoUploadProperties.getUrlPrefix())
                + "/" + AVATAR_DIRECTORY + "/" + userId + "/" + storedFilename;
    }

    private void deletePreviousAvatar(String avatarUrl, Path currentAvatarPath) {
        String avatarUrlPrefix = normalizeUrlPrefix(photoUploadProperties.getUrlPrefix())
                + "/" + AVATAR_DIRECTORY + "/";
        if (!StringUtils.hasText(avatarUrl) || !avatarUrl.startsWith(avatarUrlPrefix)) {
            return;
        }

        String relativePath = avatarUrl.substring(normalizeUrlPrefix(photoUploadProperties.getUrlPrefix()).length() + 1);
        Path avatarRoot = getUploadRoot().resolve(AVATAR_DIRECTORY).normalize();
        Path previousAvatarPath = getUploadRoot().resolve(relativePath).normalize();
        if (previousAvatarPath.startsWith(avatarRoot) && !previousAvatarPath.equals(currentAvatarPath)) {
            deleteFileQuietly(previousAvatarPath);
        }
    }

    private Path getUploadRoot() {
        return Path.of(photoUploadProperties.getBasePath()).toAbsolutePath().normalize();
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        return "image/jpg".equals(normalizedContentType) ? "image/jpeg" : normalizedContentType;
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        if (!StringUtils.hasText(urlPrefix)) {
            return "/uploads";
        }
        String normalizedUrlPrefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
        return normalizedUrlPrefix.endsWith("/")
                ? normalizedUrlPrefix.substring(0, normalizedUrlPrefix.length() - 1)
                : normalizedUrlPrefix;
    }

    private void deleteFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("删除旧头像失败: path={}", path, exception);
        }
    }
}
