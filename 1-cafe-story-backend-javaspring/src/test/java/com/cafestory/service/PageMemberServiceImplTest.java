package com.cafestory.service;

import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.mapper.PageMemberMapper;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.service.serviceImplement.PageMemberServiceImpl;
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
class PageMemberServiceImplTest {

    @Mock
    private PageMemberRepository pageMemberRepository;

    @Mock
    private PageMemberMapper pageMemberMapper;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private PageMemberServiceImpl pageMemberService;

    @Test
    void requestToJoinPage_success_pending_TC001() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, UUID.randomUUID());
        User user = user(userId);
        PageMember savedMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.PENDING);
        PageMemberResponseDTO response = response(cafePageId, userId, PageMemberStatus.PENDING);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)).thenReturn(Optional.empty());
        when(pageMemberRepository.save(any(PageMember.class))).thenReturn(savedMember);
        when(pageMemberMapper.toPageMemberResponseDTO(savedMember)).thenReturn(response);

        PageMemberResponseDTO result = pageMemberService.requestToJoinPage(cafePageId, userId);

        assertThat(result).isEqualTo(response);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void requestToJoinPage_fail_alreadyActiveMember_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, UUID.randomUUID());
        User user = user(userId);
        PageMember activeMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.ACTIVE);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)).thenReturn(Optional.of(activeMember));

        assertThatThrownBy(() -> pageMemberService.requestToJoinPage(cafePageId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(pageMemberRepository, never()).save(any(PageMember.class));
    }

    @Test
    void addPageMember_success_active_TC003() {
        UUID cafePageId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, actorUserId);
        User user = user(userId);
        PageMember savedMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.ACTIVE);
        PageMemberResponseDTO response = response(cafePageId, userId, PageMemberStatus.ACTIVE);

        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)).thenReturn(Optional.empty());
        when(pageMemberRepository.save(any(PageMember.class))).thenReturn(savedMember);
        when(pageMemberMapper.toPageMemberResponseDTO(savedMember)).thenReturn(response);

        PageMemberResponseDTO result = pageMemberService.addPageMember(cafePageId, actorUserId, userId, null);

        assertThat(result).isEqualTo(response);
        verify(cafePageValidator).validateUserCanManagePage(cafePageId, actorUserId);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void updatePageMemberStatus_success_approve_TC004() {
        UUID cafePageId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, actorUserId);
        User user = user(userId);
        PageMember pendingMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.PENDING);
        PageMemberResponseDTO response = response(cafePageId, userId, PageMemberStatus.ACTIVE);

        when(pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)).thenReturn(Optional.of(pendingMember));
        when(pageMemberRepository.save(pendingMember)).thenReturn(pendingMember);
        when(pageMemberMapper.toPageMemberResponseDTO(pendingMember)).thenReturn(response);

        PageMemberResponseDTO result = pageMemberService.updatePageMemberStatus(
                cafePageId,
                actorUserId,
                userId,
                PageMemberStatus.ACTIVE);

        assertThat(result).isEqualTo(response);
        assertThat(pendingMember.getStatus()).isEqualTo(PageMemberStatus.ACTIVE);
        verify(cafePageValidator).validateUserCanManagePage(cafePageId, actorUserId);
    }

    @Test
    void updatePageMemberStatus_fail_pendingStatus_TC005() {
        UUID cafePageId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, actorUserId);
        User user = user(userId);
        PageMember pendingMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.PENDING);

        when(pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)).thenReturn(Optional.of(pendingMember));

        assertThatThrownBy(() -> pageMemberService.updatePageMemberStatus(
                cafePageId,
                actorUserId,
                userId,
                PageMemberStatus.PENDING))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(pageMemberRepository, never()).save(any(PageMember.class));
    }

    @Test
    void getPendingPageMembers_success_TC006() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = cafePage(cafePageId, UUID.randomUUID());
        User user = user(UUID.randomUUID());
        PageMember pendingMember = pageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.PENDING);
        PageMemberResponseDTO response = response(cafePageId, user.getUserId(), PageMemberStatus.PENDING);

        when(pageMemberRepository.findByCafePageIdAndStatus(cafePageId, PageMemberStatus.PENDING))
                .thenReturn(List.of(pendingMember));
        when(pageMemberMapper.toPageMemberResponseDTO(pendingMember)).thenReturn(response);

        List<PageMemberResponseDTO> result = pageMemberService.getPendingPageMembers(cafePageId);

        assertThat(result).containsExactly(response);
        verify(cafePageValidator).validateCafePageExists(cafePageId);
    }

    private CafePage cafePage(UUID cafePageId, UUID ownerUserId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setOwner(user(ownerUserId));
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

    private PageMember pageMember(CafePage cafePage, User user, String roleName, PageMemberStatus status) {
        PageMember pageMember = new PageMember();
        pageMember.setCafePage(cafePage);
        pageMember.setUser(user);
        pageMember.setRoleName(roleName);
        pageMember.setStatus(status);
        return pageMember;
    }

    private PageMemberResponseDTO response(UUID cafePageId, UUID userId, PageMemberStatus status) {
        PageMemberResponseDTO response = new PageMemberResponseDTO();
        response.setCafePageId(cafePageId);
        response.setUserId(userId);
        response.setRoleName(PageMember.ROLE_MEMBER);
        response.setStatus(status);
        return response;
    }
}
