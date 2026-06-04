package com.cafestory.service;

import com.cafestory.dto.responseDTO.CafePageRatingResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.CafePageRating;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.service.serviceImplement.CafePageRatingServiceImpl;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafePageRatingServiceImplTest {

    @Mock
    private CafePageRatingRepository cafePageRatingRepository;

    @Mock
    private CafePageInteractionMapper cafePageInteractionMapper;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private CafePageRatingServiceImpl cafePageRatingService;

    @Test
    void rateCafePage_success_createsRating_TC001() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, PageStatus.ACTIVE, true);
        User user = user(userId);
        CafePageRating savedRating = rating(UUID.randomUUID(), cafePage, user, 4);
        CafePageRatingResponseDTO response = response(savedRating.getId(), cafePageId, userId, 4);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(cafePageRatingRepository.findByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(Optional.empty());
        when(cafePageRatingRepository.save(any(CafePageRating.class))).thenReturn(savedRating);
        when(cafePageInteractionMapper.toCafePageRatingResponseDTO(savedRating)).thenReturn(response);
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId)).thenReturn(4.5);
        when(cafePageRatingRepository.countByCafePageId(cafePageId)).thenReturn(2L);

        CafePageRatingResponseDTO result = cafePageRatingService.rateCafePage(cafePageId, userId, 4);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getRatingAverage()).isEqualTo(4.5);
        assertThat(result.getRatingCount()).isEqualTo(2L);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void rateCafePage_success_updatesExistingRating_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, PageStatus.ACTIVE, true);
        User user = user(userId);
        CafePageRating existingRating = rating(UUID.randomUUID(), cafePage, user, 2);
        CafePageRatingResponseDTO response = response(existingRating.getId(), cafePageId, userId, 5);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(cafePageRatingRepository.findByUserUserIdAndCafePageId(userId, cafePageId))
                .thenReturn(Optional.of(existingRating));
        when(cafePageRatingRepository.save(existingRating)).thenReturn(existingRating);
        when(cafePageInteractionMapper.toCafePageRatingResponseDTO(existingRating)).thenReturn(response);
        when(cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId)).thenReturn(5.0);
        when(cafePageRatingRepository.countByCafePageId(cafePageId)).thenReturn(1L);

        CafePageRatingResponseDTO result = cafePageRatingService.rateCafePage(cafePageId, userId, 5);

        assertThat(existingRating.getRating()).isEqualTo(5);
        assertThat(result.getRatingAverage()).isEqualTo(5.0);
    }

    @Test
    void rateCafePage_fail_inactiveCafePage_TC003() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(cafePageValidator.validateCafePageExists(cafePageId))
                .thenReturn(cafePage(cafePageId, PageStatus.DRAFT, false));

        assertThatThrownBy(() -> cafePageRatingService.rateCafePage(cafePageId, userId, 4))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cafe page is not available for rating"));

        verify(userValidator, never()).validateUserExists(userId);
        verify(cafePageRatingRepository, never()).save(any(CafePageRating.class));
    }

    @Test
    void rateCafePage_fail_ratingOutOfRange_TC004() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> cafePageRatingService.rateCafePage(cafePageId, userId, 6))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Rating must be between 1 and 5"));

        verify(cafePageValidator, never()).validateCafePageExists(cafePageId);
        verify(cafePageRatingRepository, never()).save(any(CafePageRating.class));
    }

    private CafePage cafePage(UUID cafePageId, PageStatus status, Boolean pageActive) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setOwner(user(UUID.randomUUID()));
        cafePage.setName("Cafe Story");
        cafePage.setAddress("123 Nguyen Hue");
        cafePage.setStatus(status);
        cafePage.setPageActive(pageActive);
        return cafePage;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("tester");
        user.setUserPassword("123456");
        user.setUserEmail("tester@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private CafePageRating rating(UUID id, CafePage cafePage, User user, Integer ratingValue) {
        CafePageRating cafePageRating = new CafePageRating();
        cafePageRating.setId(id);
        cafePageRating.setCafePage(cafePage);
        cafePageRating.setUser(user);
        cafePageRating.setRating(ratingValue);
        return cafePageRating;
    }

    private CafePageRatingResponseDTO response(UUID id, UUID cafePageId, UUID userId, Integer ratingValue) {
        CafePageRatingResponseDTO response = new CafePageRatingResponseDTO();
        response.setId(id);
        response.setCafePageId(cafePageId);
        response.setUserId(userId);
        response.setRating(ratingValue);
        return response;
    }
}
