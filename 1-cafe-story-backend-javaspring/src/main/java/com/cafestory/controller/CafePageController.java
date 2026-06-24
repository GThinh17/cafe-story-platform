package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogCursorPageResponseDTO;
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
    public List<CafePageResponseDTO> getCafePages(
            @RequestParam(required = false) UUID ownerUserId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        UUID viewerUserId = optionalUserId(principal);
        if (query != null && !query.isBlank()) {
            return cafePageService.searchCafePages(query, viewerUserId);
        }
        if (ownerUserId != null) {
            return cafePageService.getCafePagesByOwnerId(ownerUserId, viewerUserId);
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return cafePageService.getActiveCafePages(viewerUserId);
        }
        return cafePageService.getAllCafePages(viewerUserId);
    }

    @GetMapping("/top")
    public List<CafePageRankingResponseDTO> getTopCafePages(
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String province,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return cafePageService.getTopCafePages(regionId, city, area, province, size, optionalUserId(principal));
    }

    @GetMapping("/{cafePageId}")
    public CafePageResponseDTO getCafePageById(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return cafePageService.getCafePageById(cafePageId, optionalUserId(principal));
    }

    @GetMapping("/{cafePageId}/blogs")
    public BlogCursorPageResponseDTO getBlogsByCafePageId(
            @PathVariable UUID cafePageId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return cafePageService.getBlogsByCafePageId(cafePageId, cursor, size, optionalUserId(principal));
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

    private UUID optionalUserId(AuthenticatedUserPrincipal principal) {
        return principal == null ? null : principal.userId();
    }
}
