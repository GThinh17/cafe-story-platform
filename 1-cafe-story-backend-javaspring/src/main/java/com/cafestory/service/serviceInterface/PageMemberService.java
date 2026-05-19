package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.entity.enums.PageMemberStatus;

import java.util.List;
import java.util.UUID;

public interface PageMemberService {
    PageMemberResponseDTO requestToJoinPage(UUID cafePageId, UUID userId);

    PageMemberResponseDTO addPageMember(UUID cafePageId, UUID actorUserId, UUID userId, String roleName);

    PageMemberResponseDTO updatePageMemberStatus(
            UUID cafePageId,
            UUID actorUserId,
            UUID userId,
            PageMemberStatus status);

    List<PageMemberResponseDTO> getPageMembers(UUID cafePageId);

    List<PageMemberResponseDTO> getPendingPageMembers(UUID cafePageId);
}
