package com.cafestory.service.serviceInterface;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String storeAvatar(MultipartFile avatarFile, String publicBaseUrl);
}
