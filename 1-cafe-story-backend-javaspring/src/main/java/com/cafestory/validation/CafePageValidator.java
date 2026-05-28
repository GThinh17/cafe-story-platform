package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PageMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

@Component
public class CafePageValidator {

    private final CafePageRepository cafePageRepository;
    private final PageMemberRepository pageMemberRepository;

    public CafePageValidator(
            CafePageRepository cafePageRepository,
            PageMemberRepository pageMemberRepository) {
        this.cafePageRepository = cafePageRepository;
        this.pageMemberRepository = pageMemberRepository;
    }

    public CafePage validateCafePageExists(UUID cafePageId) {
        validateCafePageIdNotNull(cafePageId);
        return cafePageRepository.findById(cafePageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page not found"));
    }

    public void validateUserCanCreateCafePage(UUID ownerUserId) {
        if (cafePageRepository.existsByOwnerUserId(ownerUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already owns a cafe page");
        }
    }

    public CafePage validateUserCanCreateBlogOnPage(UUID cafePageId, UUID userId) {
        CafePage cafePage = validateCafePageExists(cafePageId);
        if (cafePage.getOwner() != null && userId.equals(cafePage.getOwner().getUserId())) {
            return cafePage;
        }
        if (pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                cafePageId,
                userId,
                PageMemberStatus.ACTIVE,
                List.of(PageMember.ROLE_OWNER, PageMember.ROLE_CO_OWNER))) {
            return cafePage;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to create blog on this cafe page");
    }

    public void validateUserCanManagePage(UUID cafePageId, UUID userId) {
        CafePage cafePage = validateCafePageExists(cafePageId);
        if (cafePage.getOwner() != null && userId.equals(cafePage.getOwner().getUserId())) {
            return;
        }
        if (pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                cafePageId,
                userId,
                PageMemberStatus.ACTIVE,
                List.of(PageMember.ROLE_OWNER, PageMember.ROLE_CO_OWNER))) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to manage this cafe page");
    }

    private void validateCafePageIdNotNull(UUID cafePageId) {
        if (cafePageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cafe page id is required");
        }
    }
}
