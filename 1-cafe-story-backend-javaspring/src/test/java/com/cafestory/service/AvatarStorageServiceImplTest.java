package com.cafestory.service;

import com.cafestory.service.serviceImplement.AvatarStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AvatarStorageServiceImpl}.
 *
 * <p>Ảnh được ghi vào thư mục tạm do JUnit cấp ({@link TempDir}) nên bài kiểm thử
 * không đụng tới thư mục uploads thật của dự án và tự dọn sau khi chạy.
 */
class AvatarStorageServiceImplTest {

    @TempDir
    Path avatarDirectory;

    @Test
    void storeAvatar_success_returnsPublicUrlAndWritesFile_TC001() throws IOException {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile file = new MockMultipartFile(
                "avatar", "anh-dai-dien.PNG", "image/png", "noi-dung".getBytes(StandardCharsets.UTF_8));

        String url = service.storeAvatar(file, "https://cafestory.vn/");

        assertThat(url).startsWith("https://cafestory.vn/uploads/avatars/");
        assertThat(url).endsWith(".png");
        try (var files = Files.list(avatarDirectory)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    void storeAvatar_success_normalizesPublicPathWithoutLeadingSlash_TC002() {
        AvatarStorageServiceImpl service = service("static/avatars");
        MultipartFile file = new MockMultipartFile(
                "avatar", "a.jpg", "image/jpeg", "x".getBytes(StandardCharsets.UTF_8));

        assertThat(service.storeAvatar(file, "https://cafestory.vn"))
                .startsWith("https://cafestory.vn/static/avatars/");
    }

    @Test
    void storeAvatar_success_blankPublicPathFallsBackToDefault_TC003() {
        AvatarStorageServiceImpl service = service("   ");
        MultipartFile file = new MockMultipartFile(
                "avatar", "a.jpg", "image/jpeg", "x".getBytes(StandardCharsets.UTF_8));

        assertThat(service.storeAvatar(file, "https://cafestory.vn"))
                .contains("/uploads/avatars/");
    }

    @ParameterizedTest
    @CsvSource({
            "image/png, .png",
            "image/webp, .webp",
            "image/gif, .gif",
            "image/jpeg, .jpg"
    })
    void storeAvatar_success_derivesExtensionFromContentTypeWhenFilenameHasNone_TC004(
            String contentType, String expectedExtension) {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile file = new MockMultipartFile(
                "avatar", "khong-co-duoi", contentType, "x".getBytes(StandardCharsets.UTF_8));

        assertThat(service.storeAvatar(file, "https://cafestory.vn")).endsWith(expectedExtension);
    }

    @Test
    void storeAvatar_success_nullOriginalFilenameIsTolerated_TC005() {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile file = new MockMultipartFile(
                "avatar", null, "image/jpeg", "x".getBytes(StandardCharsets.UTF_8));

        assertThat(service.storeAvatar(file, "https://cafestory.vn")).endsWith(".jpg");
    }

    @Test
    void storeAvatar_fail_missingFile_TC006() {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile empty = new MockMultipartFile("avatar", "a.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.storeAvatar(null, "https://cafestory.vn"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar image is required");
        assertThatThrownBy(() -> service.storeAvatar(empty, "https://cafestory.vn"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar image is required");
    }

    @Test
    void storeAvatar_fail_unsupportedContentType_TC007() {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile pdf = new MockMultipartFile(
                "avatar", "a.pdf", "application/pdf", "x".getBytes(StandardCharsets.UTF_8));
        MultipartFile unknown = new MockMultipartFile(
                "avatar", "a.bin", null, "x".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.storeAvatar(pdf, "https://cafestory.vn"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar must be a JPG, PNG, WEBP, or GIF image");
        assertThatThrownBy(() -> service.storeAvatar(unknown, "https://cafestory.vn"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Avatar must be a JPG, PNG, WEBP, or GIF image");
    }

    @Test
    void storeAvatar_fail_streamErrorBecomesInternalServerError_TC008() throws IOException {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile broken = mock(MultipartFile.class);
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getContentType()).thenReturn("image/png");
        when(broken.getOriginalFilename()).thenReturn("a.png");
        when(broken.getInputStream()).thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> service.storeAvatar(broken, "https://cafestory.vn"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unable to store avatar image");
    }

    @Test
    void storeAvatar_success_closesUploadStream_TC009() throws IOException {
        AvatarStorageServiceImpl service = service("/uploads/avatars");
        MultipartFile file = new MockMultipartFile(
                "avatar", "a.png", "image/png", "noi-dung".getBytes(StandardCharsets.UTF_8));

        String url = service.storeAvatar(file, "https://cafestory.vn");
        String filename = url.substring(url.lastIndexOf('/') + 1);

        try (InputStream stored = Files.newInputStream(avatarDirectory.resolve(filename))) {
            assertThat(new String(stored.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("noi-dung");
        }
    }

    private AvatarStorageServiceImpl service(String publicPath) {
        return new AvatarStorageServiceImpl(avatarDirectory.toString(), publicPath);
    }
}
