package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.PageMemberId;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.mapper.PageMemberMapper;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.service.serviceInterface.PageMemberService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PageMemberServiceImpl implements PageMemberService {

    private final PageMemberRepository pageMemberRepository;
    private final PageMemberMapper pageMemberMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public PageMemberServiceImpl(
            PageMemberRepository pageMemberRepository,
            PageMemberMapper pageMemberMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.pageMemberRepository = pageMemberRepository;
        this.pageMemberMapper = pageMemberMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public PageMemberResponseDTO requestToJoinPage(UUID cafePageId, UUID userId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        if (isPrimaryOwner(cafePage, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already the cafe page owner");
        }

        PageMember pageMember = pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)
                .map(existingMember -> updateExistingRequest(existingMember, PageMemberStatus.PENDING))
                .orElseGet(() -> createPageMember(cafePage, user, PageMember.ROLE_MEMBER, PageMemberStatus.PENDING));

        return pageMemberMapper.toPageMemberResponseDTO(pageMemberRepository.save(pageMember));
    }

    @Override
    @Transactional
    public PageMemberResponseDTO addPageMember(UUID cafePageId, UUID actorUserId, UUID userId, String roleName) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        if (isPrimaryOwner(cafePage, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already the cafe page owner");
        }

        PageMember pageMember = pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)
                .map(existingMember -> updateExistingMember(existingMember, normalizeRole(roleName), PageMemberStatus.ACTIVE))
                .orElseGet(() -> createPageMember(cafePage, user, normalizeRole(roleName), PageMemberStatus.ACTIVE));

        return pageMemberMapper.toPageMemberResponseDTO(pageMemberRepository.save(pageMember));
    }

    @Override
    @Transactional
    public PageMemberResponseDTO updatePageMemberStatus(
            UUID cafePageId,
            UUID actorUserId,
            UUID userId,
            PageMemberStatus status) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        PageMember pageMember = pageMemberRepository.findByCafePageIdAndUserUserId(cafePageId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page member request not found"));

        if (status == PageMemberStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or REJECTED");
        }

        pageMember.setStatus(status);
        if (status == PageMemberStatus.ACTIVE && isBlank(pageMember.getRoleName())) {
            pageMember.setRoleName(PageMember.ROLE_MEMBER);
        }

        return pageMemberMapper.toPageMemberResponseDTO(pageMemberRepository.save(pageMember));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageMemberResponseDTO> getPageMembers(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        return pageMemberRepository.findByCafePageId(cafePageId)
                .stream()
                .map(pageMemberMapper::toPageMemberResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageMemberResponseDTO> getPendingPageMembers(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        return pageMemberRepository.findByCafePageIdAndStatus(cafePageId, PageMemberStatus.PENDING)
                .stream()
                .map(pageMemberMapper::toPageMemberResponseDTO)
                .toList();
    }

    private PageMember createPageMember(
            CafePage cafePage,
            User user,
            String roleName,
            PageMemberStatus status) {
        PageMember pageMember = new PageMember();
        pageMember.setId(new PageMemberId(cafePage.getId(), user.getUserId()));
        pageMember.setCafePage(cafePage);
        pageMember.setUser(user);
        pageMember.setRoleName(normalizeRole(roleName));
        pageMember.setStatus(status);
        return pageMember;
    }

    private PageMember updateExistingRequest(PageMember pageMember, PageMemberStatus status) {
        if (pageMember.getStatus() == PageMemberStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a cafe page member");
        }
        pageMember.setRoleName(PageMember.ROLE_MEMBER);
        pageMember.setStatus(status);
        return pageMember;
    }

    private PageMember updateExistingMember(PageMember pageMember, String roleName, PageMemberStatus status) {
        pageMember.setRoleName(roleName);
        pageMember.setStatus(status);
        return pageMember;
    }

    private String normalizeRole(String roleName) {
        if (isBlank(roleName)) {
            return PageMember.ROLE_MEMBER;
        }
        String normalizedRole = roleName.trim().toUpperCase();
        if (!List.of(PageMember.ROLE_OWNER, PageMember.ROLE_CO_OWNER, PageMember.ROLE_MEMBER).contains(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cafe page member role");
        }
        return normalizedRole;
    }

    private boolean isPrimaryOwner(CafePage cafePage, UUID userId) {
        return cafePage.getOwner() != null && userId.equals(cafePage.getOwner().getUserId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
