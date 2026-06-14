package com.cafestory.service.serviceImplement;

import com.cafestory.service.serviceInterface.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarStorageServiceImpl implements AvatarStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif");

    private final Path avatarDirectory;
    private final String avatarPublicPath;

    public AvatarStorageServiceImpl(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDirectory,
            @Value("${app.upload.avatar-public-path:/uploads/avatars}") String avatarPublicPath) {
        this.avatarDirectory = Paths.get(avatarDirectory).toAbsolutePath().normalize();
        this.avatarPublicPath = normalizePublicPath(avatarPublicPath);
    }

    @Override
    public String storeAvatar(MultipartFile avatarFile, String publicBaseUrl) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar image is required");
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar must be a JPG, PNG, WEBP, or GIF image");
        }

        try {
            Files.createDirectories(avatarDirectory);
            String extension = resolveExtension(avatarFile);
            String filename = UUID.randomUUID() + extension;
            Path targetFile = avatarDirectory.resolve(filename).normalize();

            if (!targetFile.startsWith(avatarDirectory)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar filename is invalid");
            }

            try (InputStream inputStream = avatarFile.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return publicBaseUrl.replaceAll("/$", "") + avatarPublicPath + "/" + filename;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store avatar image");
        }
    }

    private String resolveExtension(MultipartFile avatarFile) {
        String originalFilename = StringUtils.cleanPath(
                avatarFile.getOriginalFilename() == null ? "" : avatarFile.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || extension.isBlank()) {
            return switch (avatarFile.getContentType()) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> ".jpg";
            };
        }

        return "." + extension.toLowerCase(Locale.ROOT);
    }

    private String normalizePublicPath(String publicPath) {
        String normalizedPath = publicPath == null || publicPath.isBlank()
                ? "/uploads/avatars"
                : publicPath.trim();

        return normalizedPath.startsWith("/") ? normalizedPath : "/" + normalizedPath;
    }
}
