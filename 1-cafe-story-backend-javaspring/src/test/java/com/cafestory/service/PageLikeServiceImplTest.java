package com.cafestory.service;

import com.cafestory.dto.responseDTO.PageLikeResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageLike;
import com.cafestory.entity.User;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.PageLikeRepository;
import com.cafestory.service.serviceImplement.PageLikeServiceImpl;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageLikeServiceImplTest {

    @Mock
    private PageLikeRepository pageLikeRepository;

    @Mock
    private CafePageInteractionMapper cafePageInteractionMapper;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private PageLikeServiceImpl pageLikeService;

    @Test
    void likePage_success_TC001() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId);
        User user = user(userId);
        PageLike savedLike = pageLike(UUID.randomUUID(), cafePage, user);
        PageLikeResponseDTO response = response(savedLike.getId(), cafePageId, userId);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageLikeRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(false);
        when(pageLikeRepository.save(any(PageLike.class))).thenReturn(savedLike);
        when(cafePageInteractionMapper.toPageLikeResponseDTO(savedLike)).thenReturn(response);

        PageLikeResponseDTO result = pageLikeService.likePage(cafePageId, userId);

        assertThat(result).isEqualTo(response);
        assertThat(cafePage.getLikeCount()).isEqualTo(1);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void likePage_fail_duplicateLike_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage(cafePageId));
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageLikeRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(true);

        assertThatThrownBy(() -> pageLikeService.likePage(cafePageId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cafe page already liked by user"));

        verify(pageLikeRepository, never()).save(any(PageLike.class));
    }

    @Test
    void unlikePage_success_TC003() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId);
        cafePage.setLikeCount(2);
        User user = user(userId);
        PageLike pageLike = pageLike(UUID.randomUUID(), cafePage, user);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageLikeRepository.findByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(Optional.of(pageLike));

        pageLikeService.unlikePage(cafePageId, userId);

        assertThat(cafePage.getLikeCount()).isEqualTo(1);
        verify(cafePageValidator).validateCafePageExists(cafePageId);
        verify(userValidator).validateUserActive(user);
        verify(pageLikeRepository).delete(pageLike);
    }

    @Test
    void unlikePage_fail_likeNotFound_TC004() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageLikeRepository.findByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pageLikeService.unlikePage(cafePageId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cafe page like not found"));
    }

    @Test
    void getLikesByCafePageId_success_TC005() {
        UUID cafePageId = UUID.randomUUID();
        PageLike pageLike = pageLike(UUID.randomUUID(), cafePage(cafePageId), user(UUID.randomUUID()));
        PageLikeResponseDTO response = response(pageLike.getId(), cafePageId, pageLike.getUser().getUserId());

        when(pageLikeRepository.findByCafePageId(cafePageId)).thenReturn(List.of(pageLike));
        when(cafePageInteractionMapper.toPageLikeResponseDTO(pageLike)).thenReturn(response);

        List<PageLikeResponseDTO> result = pageLikeService.getLikesByCafePageId(cafePageId);

        assertThat(result).containsExactly(response);
        verify(cafePageValidator).validateCafePageExists(cafePageId);
    }

    @Test
    void getLikesByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        PageLike pageLike = pageLike(UUID.randomUUID(), cafePage(UUID.randomUUID()), user(userId));
        PageLikeResponseDTO response = response(pageLike.getId(), pageLike.getCafePage().getId(), userId);

        when(pageLikeRepository.findByUserUserId(userId)).thenReturn(List.of(pageLike));
        when(cafePageInteractionMapper.toPageLikeResponseDTO(pageLike)).thenReturn(response);

        List<PageLikeResponseDTO> result = pageLikeService.getLikesByUserId(userId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(userId);
    }

    private CafePage cafePage(UUID cafePageId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setName("Cafe Story");
        cafePage.setAddress("123 Nguyen Hue");
        cafePage.setLikeCount(0);
        cafePage.setFollowerCount(0);
        return cafePage;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserPassword("123456");
        user.setUserEmail("luan@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private PageLike pageLike(UUID id, CafePage cafePage, User user) {
        PageLike pageLike = new PageLike();
        pageLike.setId(id);
        pageLike.setCafePage(cafePage);
        pageLike.setUser(user);
        return pageLike;
    }

    private PageLikeResponseDTO response(UUID id, UUID cafePageId, UUID userId) {
        PageLikeResponseDTO response = new PageLikeResponseDTO();
        response.setId(id);
        response.setCafePageId(cafePageId);
        response.setUserId(userId);
        return response;
    }
}
