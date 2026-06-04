package com.cafestory.controller;

import com.cafestory.dto.requestDTO.CafePageRatingRequestDTO;
import com.cafestory.dto.responseDTO.CafePageRatingResponseDTO;
import com.cafestory.service.serviceInterface.CafePageRatingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/cafe-pages")
public class CafePageRatingController {

    private final CafePageRatingService cafePageRatingService;

    public CafePageRatingController(CafePageRatingService cafePageRatingService) {
        this.cafePageRatingService = cafePageRatingService;
    }

    @PutMapping("/{cafePageId}/rating")
    public CafePageRatingResponseDTO rateCafePage(
            @PathVariable UUID cafePageId,
            @Valid @RequestBody CafePageRatingRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return cafePageRatingService.rateCafePage(cafePageId, requireUserId(principal), request.getRating());
    }

    @DeleteMapping("/{cafePageId}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(
            @PathVariable UUID cafePageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        cafePageRatingService.deleteRating(cafePageId, requireUserId(principal));
    }

    @GetMapping("/{cafePageId}/ratings")
    public List<CafePageRatingResponseDTO> getRatingsByCafePageId(@PathVariable UUID cafePageId) {
        return cafePageRatingService.getRatingsByCafePageId(cafePageId);
    }

    @GetMapping("/ratings/users/{userId}")
    public List<CafePageRatingResponseDTO> getRatingsByUserId(@PathVariable UUID userId) {
        return cafePageRatingService.getRatingsByUserId(userId);
    }
}
