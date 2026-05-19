package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PageMemberRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafePageValidatorTest {

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private PageMemberRepository pageMemberRepository;

    @InjectMocks
    private CafePageValidator cafePageValidator;

    @Test
    void validateCafePageExists_success_TC001() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));

        CafePage result = cafePageValidator.validateCafePageExists(cafePage.getId());

        assertThat(result).isEqualTo(cafePage);
    }

    @Test
    void validateCafePageExists_fail_nullCafePageId_TC002() {
        assertThatThrownBy(() -> cafePageValidator.validateCafePageExists(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateCafePageExists_fail_cafePageNotFound_TC003() {
        UUID cafePageId = UUID.randomUUID();

        when(cafePageRepository.findById(cafePageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cafePageValidator.validateCafePageExists(cafePageId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void validateUserCanCreateCafePage_success_TC004() {
        UUID ownerUserId = UUID.randomUUID();

        when(cafePageRepository.existsByOwnerUserId(ownerUserId)).thenReturn(false);

        cafePageValidator.validateUserCanCreateCafePage(ownerUserId);
    }

    @Test
    void validateUserCanCreateCafePage_fail_userAlreadyOwnsPage_TC005() {
        UUID ownerUserId = UUID.randomUUID();

        when(cafePageRepository.existsByOwnerUserId(ownerUserId)).thenReturn(true);

        assertThatThrownBy(() -> cafePageValidator.validateUserCanCreateCafePage(ownerUserId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User already owns a cafe page"));
    }

    @Test
    void validateUserCanCreateBlogOnPage_success_owner_TC006() {
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(userId);

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));

        cafePageValidator.validateUserCanCreateBlogOnPage(cafePage.getId(), userId);
    }

    @Test
    void validateUserCanCreateBlogOnPage_success_coOwner_TC007() {
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                eq(cafePage.getId()),
                eq(userId),
                eq(com.cafestory.entity.enums.PageMemberStatus.ACTIVE),
                anyList())).thenReturn(true);

        cafePageValidator.validateUserCanCreateBlogOnPage(cafePage.getId(), userId);
    }

    @Test
    void validateUserCanCreateBlogOnPage_fail_notOwnerOrCoOwner_TC008() {
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                eq(cafePage.getId()),
                eq(userId),
                eq(com.cafestory.entity.enums.PageMemberStatus.ACTIVE),
                anyList())).thenReturn(false);

        assertThatThrownBy(() -> cafePageValidator.validateUserCanCreateBlogOnPage(cafePage.getId(), userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User is not allowed to create blog on this cafe page"));
    }

    @Test
    void validateUserCanManagePage_success_activeCoOwner_TC009() {
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                eq(cafePage.getId()),
                eq(userId),
                eq(com.cafestory.entity.enums.PageMemberStatus.ACTIVE),
                anyList())).thenReturn(true);

        cafePageValidator.validateUserCanManagePage(cafePage.getId(), userId);
    }

    @Test
    void validateUserCanManagePage_fail_notManager_TC010() {
        UUID userId = UUID.randomUUID();
        CafePage cafePage = cafePage(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));
        when(pageMemberRepository.existsByCafePageIdAndUserUserIdAndStatusAndRoleNameIn(
                eq(cafePage.getId()),
                eq(userId),
                eq(com.cafestory.entity.enums.PageMemberStatus.ACTIVE),
                anyList())).thenReturn(false);

        assertThatThrownBy(() -> cafePageValidator.validateUserCanManagePage(cafePage.getId(), userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User is not allowed to manage this cafe page"));
    }

    private CafePage cafePage(UUID ownerUserId) {
        User owner = new User();
        owner.setUserId(ownerUserId);

        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setOwner(owner);
        return cafePage;
    }
}
