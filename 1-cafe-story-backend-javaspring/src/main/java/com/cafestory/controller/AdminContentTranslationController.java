package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import com.cafestory.dto.responseDTO.AdminContentTranslationResponseDTO;
import com.cafestory.service.serviceInterface.AdminContentTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/translations")
@RequiredArgsConstructor
public class AdminContentTranslationController {

    private final AdminContentTranslationService translationService;

    @PostMapping
    public AdminContentTranslationResponseDTO translate(
            @Valid @RequestBody AdminContentTranslationRequestDTO request) {
        return translationService.translate(request);
    }
}
