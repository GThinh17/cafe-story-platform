package com.cafestory.service;

import com.cafestory.dto.responseDTO.PageFollowResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.User;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.service.serviceImplement.PageFollowServiceImpl;
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
class PageFollowServiceImplTest {

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private CafePageInteractionMapper cafePageInteractionMapper;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PageMemberRepository pageMemberRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PageFollowServiceImpl pageFollowService;

    @Test
    void followPage_success_TC001() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId);
        User user = user(userId);
        PageFollow savedFollow = pageFollow(UUID.randomUUID(), cafePage, user);
        PageFollowResponseDTO response = response(savedFollow.getId(), cafePageId, userId);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageFollowRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(false);
        when(pageFollowRepository.save(any(PageFollow.class))).thenReturn(savedFollow);
        when(cafePageInteractionMapper.toPageFollowResponseDTO(savedFollow)).thenReturn(response);

        PageFollowResponseDTO result = pageFollowService.followPage(cafePageId, userId);

        assertThat(result).isEqualTo(response);
        assertThat(cafePage.getFollowerCount()).isEqualTo(1);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void followPage_fail_duplicateFollow_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage(cafePageId));
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageFollowRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(true);

        assertThatThrownBy(() -> pageFollowService.followPage(cafePageId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cafe page already followed by user"));

        verify(pageFollowRepository, never()).save(any(PageFollow.class));
    }

    @Test
    void unfollowPage_success_TC003() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId);
        cafePage.setFollowerCount(2);
        User user = user(userId);
        PageFollow pageFollow = pageFollow(UUID.randomUUID(), cafePage, user);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageFollowRepository.findByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(Optional.of(pageFollow));

        pageFollowService.unfollowPage(cafePageId, userId);

        assertThat(cafePage.getFollowerCount()).isEqualTo(1);
        verify(cafePageValidator).validateCafePageExists(cafePageId);
        verify(userValidator).validateUserActive(user);
        verify(pageFollowRepository).delete(pageFollow);
    }

    @Test
    void unfollowPage_fail_followNotFound_TC004() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageFollowRepository.findByUserUserIdAndCafePageId(userId, cafePageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pageFollowService.unfollowPage(cafePageId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cafe page follow not found"));
    }

    @Test
    void getFollowersByCafePageId_success_TC005() {
        UUID cafePageId = UUID.randomUUID();
        PageFollow pageFollow = pageFollow(UUID.randomUUID(), cafePage(cafePageId), user(UUID.randomUUID()));
        PageFollowResponseDTO response = response(pageFollow.getId(), cafePageId, pageFollow.getUser().getUserId());

        when(pageFollowRepository.findByCafePageId(cafePageId)).thenReturn(List.of(pageFollow));
        when(cafePageInteractionMapper.toPageFollowResponseDTO(pageFollow)).thenReturn(response);

        List<PageFollowResponseDTO> result = pageFollowService.getFollowersByCafePageId(cafePageId);

        assertThat(result).containsExactly(response);
        verify(cafePageValidator).validateCafePageExists(cafePageId);
    }

    @Test
    void getFollowedPagesByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        PageFollow pageFollow = pageFollow(UUID.randomUUID(), cafePage(UUID.randomUUID()), user(userId));
        PageFollowResponseDTO response = response(pageFollow.getId(), pageFollow.getCafePage().getId(), userId);

        when(pageFollowRepository.findByUserUserId(userId)).thenReturn(List.of(pageFollow));
        when(cafePageInteractionMapper.toPageFollowResponseDTO(pageFollow)).thenReturn(response);

        List<PageFollowResponseDTO> result = pageFollowService.getFollowedPagesByUserId(userId);

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

    private PageFollow pageFollow(UUID id, CafePage cafePage, User user) {
        PageFollow pageFollow = new PageFollow();
        pageFollow.setId(id);
        pageFollow.setCafePage(cafePage);
        pageFollow.setUser(user);
        return pageFollow;
    }

    private PageFollowResponseDTO response(UUID id, UUID cafePageId, UUID userId) {
        PageFollowResponseDTO response = new PageFollowResponseDTO();
        response.setId(id);
        response.setCafePageId(cafePageId);
        response.setUserId(userId);
        return response;
    }
}
