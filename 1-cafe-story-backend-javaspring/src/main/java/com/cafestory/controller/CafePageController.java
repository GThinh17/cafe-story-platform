package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageRankingResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.service.serviceInterface.CafePageService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/cafe-pages")
public class CafePageController {

    private final CafePageService cafePageService;

    public CafePageController(CafePageService cafePageService) {
        this.cafePageService = cafePageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CafePageResponseDTO createCafePage(
            @Valid @RequestBody CafePageCreateDTO cafePageCreateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        cafePageCreateDTO.setOwnerUserId(requireUserId(principal));
        return cafePageService.createCafePage(cafePageCreateDTO);
    }

    @GetMapping
    public List<CafePageResponseDTO> getCafePages(@RequestParam(required = false) UUID ownerUserId) {
        if (ownerUserId != null) {
            return cafePageService.getCafePagesByOwnerId(ownerUserId);
        }
        return cafePageService.getAllCafePages();
    }

    @GetMapping("/top")
    public List<CafePageRankingResponseDTO> getTopCafePages(
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "10") int size) {
        return cafePageService.getTopCafePages(regionId, city, size);
    }

    @GetMapping("/{cafePageId}")
    public CafePageResponseDTO getCafePageById(@PathVariable UUID cafePageId) {
        return cafePageService.getCafePageById(cafePageId);
    }

    @GetMapping("/{cafePageId}/blogs")
    public List<BlogResponseDTO> getBlogsByCafePageId(@PathVariable UUID cafePageId) {
        return cafePageService.getBlogsByCafePageId(cafePageId);
    }

    @PatchMapping("/{cafePageId}")
    public CafePageResponseDTO updateCafePage(
            @PathVariable UUID cafePageId,
            @Valid @RequestBody CafePageUpdateDTO cafePageUpdateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return cafePageService.updateCafePage(cafePageId, requireUserId(principal), cafePageUpdateDTO);
    }

    @DeleteMapping("/{cafePageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCafePage(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        cafePageService.deleteCafePage(cafePageId, requireUserId(principal));
    }
}
