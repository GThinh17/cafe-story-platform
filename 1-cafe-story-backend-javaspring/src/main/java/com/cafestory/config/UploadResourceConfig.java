package com.cafestory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final Path uploadRootDirectory;

    public UploadResourceConfig(@Value("${app.upload.root-dir:uploads}") String uploadRootDirectory) {
        this.uploadRootDirectory = Paths.get(uploadRootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadRootDirectory.toUri().toString() + "/");
    }
}
