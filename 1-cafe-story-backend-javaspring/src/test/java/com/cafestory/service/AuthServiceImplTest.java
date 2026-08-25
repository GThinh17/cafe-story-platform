package com.cafestory.service;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.UsernameSuggestionResponse;
import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceImplement.AuthServiceImpl;
import com.cafestory.service.serviceInterface.RefreshTokenService;
import com.cafestory.until.security.JwtService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserValidator userValidator;

    @Test
    void register_success_assignsDefaultUserRole_TC001() {
        RegisterRequest request = registerRequest();
        Role userRole = role("USER");

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(UUID.randomUUID());
            return user;
        });
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRoleAssignmentRepository.findByUserUserId(any(UUID.class)))
                .thenReturn(List.of(assignment(user("luan123@example.com", "luan123"), userRole)));
        when(jwtService.createAccessToken(any(User.class), any())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = service().register(request);

        assertThat(response.getUser().getUserEmail()).isEqualTo(request.getUserEmail());
        assertThat(response.getUser().getRoles()).containsExactly("USER");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserPassword()).isEqualTo("encoded-password");
        verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
        verify(jwtService).createAccessToken(any(User.class), any());
        verify(refreshTokenService).createRefreshToken(any(User.class));
    }

    @Test
    void register_fail_duplicateEmail_TC002() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(user(
                request.getUserEmail(),
                request.getUserName())));

        assertThatThrownBy(() -> service().register(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_fail_duplicateUsername_TC003() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(user(
                request.getUserEmail(),
                request.getUserName())));

        assertThatThrownBy(() -> service().register(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success_setsTokensInResponse_TC004() {
        LoginRequest request = loginRequest();
        User user = user("luan123@example.com", "luan123");
        Role role = role("USER");

        when(userRepository.findByUserEmailOrUserName(request.getIdentifier(), request.getIdentifier()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getUserPassword())).thenReturn(true);
        when(userRoleAssignmentRepository.findByUserUserId(user.getUserId()))
                .thenReturn(List.of(assignment(user, role)));
        when(jwtService.createAccessToken(user, List.of("USER"))).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = service().login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getRoles()).containsExactly("USER");
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void login_fail_wrongPassword_TC005() {
        LoginRequest request = loginRequest();
        User user = user("luan123@example.com", "luan123");

        when(userRepository.findByUserEmailOrUserName(request.getIdentifier(), request.getIdentifier()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getUserPassword())).thenReturn(false);

        assertThatThrownBy(() -> service().login(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void refresh_success_issuesNewAccessToken_TC006() {
        User user = user("luan123@example.com", "luan123");
        Role role = role("USER");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        when(refreshTokenService.validateRefreshToken("refresh-token")).thenReturn(refreshToken);
        when(userRoleAssignmentRepository.findByUserUserId(user.getUserId()))
                .thenReturn(List.of(assignment(user, role)));
        when(jwtService.createAccessToken(user, List.of("USER"))).thenReturn("new-access-token");

        AuthResponse response = service().refresh("refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNull();
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void logout_success_revokesRefreshToken_TC007() {
        service().logout("refresh-token");

        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }

    @Test
    void suggestUserNames_vietnameseDiacritics_returnsNormalizedAvailableSuggestions_TC008() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("Phạm Thanh Vũ");

        assertThat(response.getSuggestions()).contains("thanh.vu", "pham.vu", "phamvu");
        assertThat(response.getSuggestions()).hasSizeBetween(4, 7);
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
        verify(userRepository, times(1)).findExistingUserNamesLowercase(any());
    }

    @Test
    void suggestUserNames_shortName_returnsReadableSuggestions_TC009() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("Kiều Mị");

        assertThat(response.getSuggestions()).contains("kieumi", "kieu.mi");
        assertThat(response.getSuggestions()).hasSizeBetween(4, 7);
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
    }

    @Test
    void suggestUserNames_fiveTokenName_prefersFirstLastAndPenultimateLast_TC010() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("Nguyễn Thị Minh Thanh Vũ");

        assertThat(response.getSuggestions()).contains("nguyen.vu", "thanh.vu");
        assertThat(response.getSuggestions()).doesNotContain("thi.vu");
        assertThat(response.getSuggestions()).hasSizeBetween(4, 7);
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
    }

    @Test
    void suggestUserNames_existingUsername_filtersExistingCandidate_TC011() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of("thanh.vu"));

        UsernameSuggestionResponse response = service().suggestUserNames("Phạm Thanh Vũ");

        assertThat(response.getSuggestions()).doesNotContain("thanh.vu");
        assertThat(response.getSuggestions()).hasSizeBetween(4, 7);
        verify(userRepository, times(1)).findExistingUserNamesLowercase(any());
    }

    @Test
    void suggestUserNames_reservedUsername_filtersReservedCandidate_TC012() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("admin");

        assertThat(response.getSuggestions()).doesNotContain("admin");
        assertThat(response.getSuggestions()).hasSizeBetween(4, 7);
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
    }

    @Test
    void suggestUserNames_invalidSeparators_filtersInvalidCandidates_TC013() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("A.. B__");

        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
        assertThat(response.getSuggestions()).noneMatch(candidate -> candidate.contains("..") || candidate.contains("__"));
    }

    @Test
    void suggestUserNames_blankFullName_throwsBadRequest_TC014() {
        assertThatThrownBy(() -> service().suggestUserNames("   "))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("Full name is required");

        verify(userRepository, never()).findExistingUserNamesLowercase(any());
    }

    @Test
    void suggestUserNames_nullFullName_throwsBadRequest_TC015() {
        assertThatThrownBy(() -> service().suggestUserNames(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Full name is required");
    }

    @Test
    void suggestUserNames_nameWithoutLatinLetters_stillReturnsSuggestions_TC016() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        // Tên chỉ gồm ký tự bị lọc sạch: bộ sinh phải rơi về tiền tố "user".
        UsernameSuggestionResponse response = service().suggestUserNames("!!! ???");

        assertThat(response.getSuggestions()).isNotEmpty();
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
        assertThat(response.getSuggestions()).allMatch(candidate -> candidate.startsWith("user"));
    }

    @Test
    void suggestUserNames_singleTokenName_skipsPenultimateCandidates_TC017() {
        when(userRepository.findExistingUserNamesLowercase(any())).thenReturn(List.of());

        UsernameSuggestionResponse response = service().suggestUserNames("Vu");

        assertThat(response.getSuggestions()).isNotEmpty();
        assertThat(response.getSuggestions()).allMatch(this::isValidSuggestion);
    }

    @Test
    void getCurrentUser_success_returnsProfileWithoutTokens_TC018() {
        UUID userId = UUID.randomUUID();
        User user = user("luan123@example.com", "luan123");
        user.setUserId(userId);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(userRoleAssignmentRepository.findByUserUserId(userId))
                .thenReturn(List.of(assignment(user, role("USER"))));

        AuthResponse response = service().getCurrentUser(userId);

        assertThat(response.getUser().getUserId()).isEqualTo(userId);
        assertThat(response.getUser().getRoles()).containsExactly("USER");
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        verify(userValidator).validateUserActive(user);
    }

    private AuthServiceImpl service() {
        return new AuthServiceImpl(
                userRepository,
                cafePageRepository,
                roleRepository,
                userRoleAssignmentRepository,
                passwordEncoder,
                jwtService,
                refreshTokenService,
                userValidator);
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUserName("luan123");
        request.setUserFullName("Nguyen Van Luan");
        request.setPassword("123456");
        request.setUserEmail("luan123@example.com");
        request.setUserPhone(987654321L);
        request.setUserAvatar("https://example.com/avatar.png");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("luan123@example.com");
        request.setPassword("123456");
        return request;
    }

    private User user(String email, String username) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(username);
        user.setUserFullName("Nguyen Van Luan");
        user.setUserPassword("encoded-password");
        user.setUserEmail(email);
        user.setAccountStatus(true);
        return user;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setId(1);
        role.setName(name);
        return role;
    }

    private UserRoleAssignment assignment(User user, Role role) {
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        return assignment;
    }

    private boolean isValidSuggestion(String suggestion) {
        return suggestion.matches("^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$");
    }
}
