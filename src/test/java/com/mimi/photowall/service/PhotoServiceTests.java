package com.mimi.photowall.service;

import com.mimi.photowall.common.UserPrincipal;
import com.mimi.photowall.config.PhotoUploadProperties;
import com.mimi.photowall.entity.Photo;
import com.mimi.photowall.mapper.PhotoMapper;
import com.mimi.photowall.vo.PhotoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PhotoServiceTests {

    private static final Long CURRENT_USER_ID = 1001L;

    @TempDir
    private Path uploadRoot;

    private PhotoMapper photoMapper;

    private PhotoService photoService;

    @BeforeEach
    void setUp() {
        photoMapper = mock(PhotoMapper.class);
        PhotoUploadProperties properties = new PhotoUploadProperties();
        properties.setBasePath(uploadRoot.toString());
        properties.setUrlPrefix("/uploads");
        properties.setThumbnailMaxSide(120);
        properties.setMaxBatchCount(20);
        photoService = new PhotoService(photoMapper, properties);
        mockCurrentUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadPhotosShouldPersistImageMetadataAndFallbackTakenTime() throws IOException {
        doAnswer(invocation -> {
            Photo photo = invocation.getArgument(0);
            photo.setId(1L);
            return 1;
        }).when(photoMapper).insert(any(Photo.class));

        LocalDateTime beforeUpload = LocalDateTime.now().minusSeconds(1);
        List<PhotoVO> photos = photoService.uploadPhotos(new MultipartFile[]{jpegFile("family.jpg")});
        LocalDateTime afterUpload = LocalDateTime.now().plusSeconds(1);

        assertThat(photos).hasSize(1);
        PhotoVO photoVO = photos.get(0);
        assertThat(photoVO.getId()).isEqualTo(1L);
        assertThat(photoVO.getUserId()).isEqualTo(CURRENT_USER_ID);
        assertThat(photoVO.getOriginalFilename()).isEqualTo("family.jpg");
        assertThat(photoVO.getContentType()).isEqualTo("image/jpeg");
        assertThat(photoVO.getWidth()).isEqualTo(80);
        assertThat(photoVO.getHeight()).isEqualTo(60);
        assertThat(photoVO.getUrl()).startsWith("/uploads/originals/");
        assertThat(photoVO.getThumbnailUrl()).startsWith("/uploads/thumbnails/");
        assertThat(photoVO.getUploadTime()).isBetween(beforeUpload, afterUpload);
        assertThat(Duration.between(photoVO.getUploadTime(), photoVO.getTakenTime()).abs()).isLessThan(Duration.ofSeconds(1));

        ArgumentCaptor<Photo> photoCaptor = ArgumentCaptor.forClass(Photo.class);
        verify(photoMapper).insert(photoCaptor.capture());
        Photo savedPhoto = photoCaptor.getValue();
        assertThat(Files.exists(Path.of(savedPhoto.getStoragePath()))).isTrue();
        assertThat(Files.exists(Path.of(savedPhoto.getThumbnailPath()))).isTrue();
    }

    @Test
    void uploadPhotosShouldCleanupCreatedFilesWhenPersistenceFails() throws IOException {
        doThrow(new RuntimeException("insert failed")).when(photoMapper).insert(any(Photo.class));

        assertThatThrownBy(() -> photoService.uploadPhotos(new MultipartFile[]{jpegFile("broken.jpg")}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("insert failed");

        try (var paths = Files.walk(uploadRoot)) {
            assertThat(paths.filter(Files::isRegularFile)).isEmpty();
        }
    }

    private MockMultipartFile jpegFile(String filename) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage image = new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(180, 82, 65));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "jpg", outputStream);
        return new MockMultipartFile("files", filename, "image/jpeg", outputStream.toByteArray());
    }

    private void mockCurrentUser() {
        UserPrincipal principal = new UserPrincipal();
        principal.setUserId(CURRENT_USER_ID);
        principal.setUsername("photo-user");
        principal.setRoles(List.of("USER"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
