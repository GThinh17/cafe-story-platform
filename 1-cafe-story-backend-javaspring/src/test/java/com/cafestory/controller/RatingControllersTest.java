package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogRatingRequestDTO;
import com.cafestory.dto.requestDTO.CafePageRatingRequestDTO;
import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;
import com.cafestory.dto.responseDTO.CafePageRatingResponseDTO;
import com.cafestory.service.serviceInterface.BlogRatingService;
import com.cafestory.service.serviceInterface.CafePageRatingService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử hai bộ điều khiển chấm điểm — {@link BlogRatingController} và
 * {@link CafePageRatingController}. Cả hai cùng một khuôn: điểm nằm trong thân
 * yêu cầu, còn người chấm luôn lấy từ token.
 */
class RatingControllersTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class BlogRatingControllerTest {

        @Mock
        private BlogRatingService blogRatingService;

        @InjectMocks
        private BlogRatingController blogRatingController;

        private AuthenticatedUserPrincipal principal;

        @BeforeEach
        void setUp() {
            principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "an", List.of("USER"));
        }

        @Test
        void rateBlog_success_usesTokenUserId_TC001() {
            UUID blogId = UUID.randomUUID();
            BlogRatingRequestDTO request = new BlogRatingRequestDTO();
            request.setRating(4);
            BlogRatingResponseDTO response = new BlogRatingResponseDTO();
            when(blogRatingService.rateBlog(blogId, principal.userId(), 4)).thenReturn(response);

            assertThat(blogRatingController.rateBlog(blogId, request, principal)).isSameAs(response);
        }

        @Test
        void deleteRating_success_usesTokenUserId_TC002() {
            UUID blogId = UUID.randomUUID();

            blogRatingController.deleteRating(blogId, principal);

            verify(blogRatingService).deleteRating(blogId, principal.userId());
        }

        @Test
        void getRatingsByBlogId_success_delegates_TC003() {
            UUID blogId = UUID.randomUUID();
            List<BlogRatingResponseDTO> ratings = List.of(new BlogRatingResponseDTO());
            when(blogRatingService.getRatingsByBlogId(blogId)).thenReturn(ratings);

            assertThat(blogRatingController.getRatingsByBlogId(blogId)).isSameAs(ratings);
        }

        @Test
        void getRatingsByUserId_success_delegates_TC004() {
            UUID userId = UUID.randomUUID();
            List<BlogRatingResponseDTO> ratings = List.of(new BlogRatingResponseDTO());
            when(blogRatingService.getRatingsByUserId(userId)).thenReturn(ratings);

            assertThat(blogRatingController.getRatingsByUserId(userId)).isSameAs(ratings);
        }

        @Test
        void deleteRating_fail_missingPrincipal_TC005() {
            UUID blogId = UUID.randomUUID();

            assertThatThrownBy(() -> blogRatingController.deleteRating(blogId, null))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Authentication is required");
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class CafePageRatingControllerTest {

        @Mock
        private CafePageRatingService cafePageRatingService;

        @InjectMocks
        private CafePageRatingController cafePageRatingController;

        private AuthenticatedUserPrincipal principal;

        @BeforeEach
        void setUp() {
            principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "an", List.of("USER"));
        }

        @Test
        void rateCafePage_success_usesTokenUserId_TC006() {
            UUID cafePageId = UUID.randomUUID();
            CafePageRatingRequestDTO request = new CafePageRatingRequestDTO();
            request.setRating(5);
            CafePageRatingResponseDTO response = new CafePageRatingResponseDTO();
            when(cafePageRatingService.rateCafePage(cafePageId, principal.userId(), 5)).thenReturn(response);

            assertThat(cafePageRatingController.rateCafePage(cafePageId, request, principal)).isSameAs(response);
        }

        @Test
        void deleteRating_success_usesTokenUserId_TC007() {
            UUID cafePageId = UUID.randomUUID();

            cafePageRatingController.deleteRating(cafePageId, principal);

            verify(cafePageRatingService).deleteRating(cafePageId, principal.userId());
        }

        @Test
        void getRatingsByCafePageId_success_delegates_TC008() {
            UUID cafePageId = UUID.randomUUID();
            List<CafePageRatingResponseDTO> ratings = List.of(new CafePageRatingResponseDTO());
            when(cafePageRatingService.getRatingsByCafePageId(cafePageId)).thenReturn(ratings);

            assertThat(cafePageRatingController.getRatingsByCafePageId(cafePageId)).isSameAs(ratings);
        }

        @Test
        void getRatingsByUserId_success_delegates_TC009() {
            UUID userId = UUID.randomUUID();
            List<CafePageRatingResponseDTO> ratings = List.of(new CafePageRatingResponseDTO());
            when(cafePageRatingService.getRatingsByUserId(userId)).thenReturn(ratings);

            assertThat(cafePageRatingController.getRatingsByUserId(userId)).isSameAs(ratings);
        }
    }
}
