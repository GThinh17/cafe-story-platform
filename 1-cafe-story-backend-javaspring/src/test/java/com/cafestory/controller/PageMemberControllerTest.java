package com.cafestory.controller;

import com.cafestory.dto.requestDTO.PageMemberAddRequestDTO;
import com.cafestory.dto.requestDTO.PageMemberStatusUpdateDTO;
import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.service.serviceInterface.PageMemberService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageMemberControllerTest {

    @Mock
    private PageMemberService pageMemberService;

    @InjectMocks
    private PageMemberController pageMemberController;

    @Test
    void requestToJoinPage_success_TC001() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PageMemberResponseDTO response = response(cafePageId, userId, PageMemberStatus.PENDING);

        when(pageMemberService.requestToJoinPage(cafePageId, userId)).thenReturn(response);

        PageMemberResponseDTO result = pageMemberController.requestToJoinPage(cafePageId, principal(userId));

        assertThat(result).isEqualTo(response);
        verify(pageMemberService).requestToJoinPage(cafePageId, userId);
    }

    @Test
    void addPageMember_success_TC002() {
        UUID cafePageId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        PageMemberAddRequestDTO request = addRequest();
        PageMemberResponseDTO response = response(cafePageId, request.getUserId(), PageMemberStatus.ACTIVE);

        when(pageMemberService.addPageMember(
                cafePageId,
                actorUserId,
                request.getUserId(),
                request.getRoleName())).thenReturn(response);

        PageMemberResponseDTO result = pageMemberController.addPageMember(cafePageId, request, principal(actorUserId));

        assertThat(result).isEqualTo(response);
        verify(pageMemberService).addPageMember(
                cafePageId,
                actorUserId,
                request.getUserId(),
                request.getRoleName());
    }

    @Test
    void updatePageMemberStatus_success_TC003() {
        UUID cafePageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        PageMemberStatusUpdateDTO request = statusRequest();
        PageMemberResponseDTO response = response(cafePageId, userId, PageMemberStatus.ACTIVE);

        when(pageMemberService.updatePageMemberStatus(
                cafePageId,
                actorUserId,
                userId,
                request.getStatus())).thenReturn(response);

        PageMemberResponseDTO result =
                pageMemberController.updatePageMemberStatus(cafePageId, userId, request, principal(actorUserId));

        assertThat(result).isEqualTo(response);
        verify(pageMemberService).updatePageMemberStatus(
                cafePageId,
                actorUserId,
                userId,
                request.getStatus());
    }

    @Test
    void getPageMembers_success_TC004() {
        UUID cafePageId = UUID.randomUUID();
        List<PageMemberResponseDTO> response = List.of(response(cafePageId, UUID.randomUUID(), PageMemberStatus.ACTIVE));

        when(pageMemberService.getPageMembers(cafePageId)).thenReturn(response);

        List<PageMemberResponseDTO> result = pageMemberController.getPageMembers(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(pageMemberService).getPageMembers(cafePageId);
    }

    @Test
    void getPendingPageMembers_success_TC005() {
        UUID cafePageId = UUID.randomUUID();
        List<PageMemberResponseDTO> response = List.of(response(cafePageId, UUID.randomUUID(), PageMemberStatus.PENDING));

        when(pageMemberService.getPendingPageMembers(cafePageId)).thenReturn(response);

        List<PageMemberResponseDTO> result = pageMemberController.getPendingPageMembers(cafePageId);

        assertThat(result).isEqualTo(response);
        verify(pageMemberService).getPendingPageMembers(cafePageId);
    }

    private PageMemberAddRequestDTO addRequest() {
        PageMemberAddRequestDTO request = new PageMemberAddRequestDTO();
        request.setUserId(UUID.randomUUID());
        request.setRoleName("MEMBER");
        return request;
    }

    private PageMemberStatusUpdateDTO statusRequest() {
        PageMemberStatusUpdateDTO request = new PageMemberStatusUpdateDTO();
        request.setStatus(PageMemberStatus.ACTIVE);
        return request;
    }

    private PageMemberResponseDTO response(UUID cafePageId, UUID userId, PageMemberStatus status) {
        PageMemberResponseDTO response = new PageMemberResponseDTO();
        response.setCafePageId(cafePageId);
        response.setUserId(userId);
        response.setRoleName("MEMBER");
        response.setStatus(status);
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
