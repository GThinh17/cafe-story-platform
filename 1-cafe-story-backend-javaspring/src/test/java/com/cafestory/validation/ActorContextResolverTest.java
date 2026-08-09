package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.service.model.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link ActorContextResolver} — lớp quyết định một hành động được ghi
 * dưới danh nghĩa người dùng hay trang quán.
 */
@ExtendWith(MockitoExtension.class)
class ActorContextResolverTest {

    @Mock
    private UserValidator userValidator;
    @Mock
    private CafePageValidator cafePageValidator;

    private ActorContextResolver actorContextResolver;

    private User actor;

    @BeforeEach
    void setUp() {
        actorContextResolver = new ActorContextResolver(userValidator, cafePageValidator);
        actor = new User();
        actor.setUserId(UUID.randomUUID());
        actor.setUserName("an");
        actor.setAccountStatus(true);
    }

    @Test
    void resolve_success_defaultsToUserContextWhenTypeIsNull_TC001() {
        when(userValidator.validateUserExists(actor.getUserId())).thenReturn(actor);

        ActorContext result = actorContextResolver.resolve(actor.getUserId(), null, null);

        assertThat(result.actorUser()).isSameAs(actor);
        assertThat(result.actorContextType()).isEqualTo(ActorContextType.USER);
        assertThat(result.actorCafePage()).isNull();
        verify(userValidator).validateUserActive(actor);
        verify(cafePageValidator, never()).validateUserCanManagePage(any(), any());
    }

    @Test
    void resolve_success_explicitUserContextIgnoresCafePageId_TC002() {
        when(userValidator.validateUserExists(actor.getUserId())).thenReturn(actor);

        ActorContext result = actorContextResolver.resolve(
                actor.getUserId(), ActorContextType.USER, UUID.randomUUID());

        assertThat(result.actorContextType()).isEqualTo(ActorContextType.USER);
        assertThat(result.actorCafePage()).isNull();
    }

    @Test
    void resolve_success_cafePageContextChecksManagePermission_TC003() {
        UUID cafePageId = UUID.randomUUID();
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        when(userValidator.validateUserExists(actor.getUserId())).thenReturn(actor);
        when(cafePageValidator.validateCafePageExists(cafePageId)).thenReturn(cafePage);

        ActorContext result = actorContextResolver.resolve(
                actor.getUserId(), ActorContextType.CAFE_PAGE, cafePageId);

        assertThat(result.actorContextType()).isEqualTo(ActorContextType.CAFE_PAGE);
        assertThat(result.actorCafePage()).isSameAs(cafePage);
        verify(cafePageValidator).validateUserCanManagePage(cafePageId, actor.getUserId());
    }

    @Test
    void resolve_fail_cafePageContextWithoutCafePageId_TC004() {
        UUID actorUserId = actor.getUserId();
        when(userValidator.validateUserExists(actorUserId)).thenReturn(actor);

        assertThatThrownBy(() ->
                actorContextResolver.resolve(actorUserId, ActorContextType.CAFE_PAGE, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("actorCafePageId is required when acting as cafe page");
    }

    @Test
    void resolve_fail_actorCannotManageCafePage_TC005() {
        UUID cafePageId = UUID.randomUUID();
        UUID actorUserId = actor.getUserId();
        when(userValidator.validateUserExists(actorUserId)).thenReturn(actor);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action"))
                .when(cafePageValidator).validateUserCanManagePage(cafePageId, actorUserId);

        assertThatThrownBy(() ->
                actorContextResolver.resolve(actorUserId, ActorContextType.CAFE_PAGE, cafePageId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unauthorized action");
    }

    @Test
    void resolve_fail_inactiveActorIsRejected_TC006() {
        UUID actorUserId = actor.getUserId();
        when(userValidator.validateUserExists(actorUserId)).thenReturn(actor);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive"))
                .when(userValidator).validateUserActive(actor);

        assertThatThrownBy(() -> actorContextResolver.resolve(actorUserId, ActorContextType.USER, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User account is inactive");
    }
}
