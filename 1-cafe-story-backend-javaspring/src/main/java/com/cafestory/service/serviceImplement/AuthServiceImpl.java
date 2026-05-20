package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.AuthUserResponse;
import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.RoleRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.repository.UserRoleAssignmentRepository;
import com.cafestory.service.serviceInterface.AuthService;
import com.cafestory.service.serviceInterface.RefreshTokenService;
import com.cafestory.until.security.JwtService;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserValidator userValidator;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserValidator userValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateUniqueUserName(request.getUserName());
        validateUniqueUserEmail(request.getUserEmail());

        User user = new User();
        user.setUserName(request.getUserName());
        user.setUserFullName(request.getUserFullName());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserEmail(request.getUserEmail());
        user.setUserPhone(request.getUserPhone());
        user.setUserAvatar(request.getUserAvatar());
        user.setAccountStatus(true);

        User savedUser = userRepository.save(user);
        assignDefaultUserRole(savedUser);
        return response(savedUser, roles(savedUser.getUserId()), null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        User user = userRepository.findByUserEmailOrUserName(identifier, identifier)
                .orElseThrow(() -> unauthorized("Invalid username/email or password"));
        userValidator.validateUserActive(user);
        if (!passwordEncoder.matches(request.getPassword(), user.getUserPassword())) {
            throw unauthorized("Invalid username/email or password");
        }

        List<String> roles = roles(user.getUserId());
        String accessToken = jwtService.createAccessToken(user, roles);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return response(user, roles, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(UUID userId) {
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);
        return response(user, roles(userId), null, null);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken validRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);
        User user = validRefreshToken.getUser();
        userValidator.validateUserActive(user);
        List<String> roles = roles(user.getUserId());
        String accessToken = jwtService.createAccessToken(user, roles);
        return response(user, roles, accessToken, null);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    private void validateUniqueUserName(String userName) {
        userRepository.findByUserName(userName).ifPresent(user -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        });
    }

    private void validateUniqueUserEmail(String userEmail) {
        userRepository.findByUserEmail(userEmail).ifPresent(user -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });
    }

    private void assignDefaultUserRole(User user) {
        Role role = roleRepository.findByName(UserRole.USER.name()).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(UserRole.USER.name());
            return roleRepository.save(newRole);
        });

        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        userRoleAssignmentRepository.save(assignment);
    }

    private List<String> roles(UUID userId) {
        return userRoleAssignmentRepository.findByUserUserId(userId)
                .stream()
                .map(assignment -> assignment.getRole().getName())
                .toList();
    }

    private AuthResponse response(User user, List<String> roles, String accessToken, String refreshToken) {
        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setUserName(user.getUserName());
        userResponse.setUserFullName(user.getUserFullName());
        userResponse.setUserEmail(user.getUserEmail());
        userResponse.setUserPhone(user.getUserPhone());
        userResponse.setUserAvatar(user.getUserAvatar());
        userResponse.setAccountStatus(user.getAccountStatus());
        userResponse.setRoles(roles);

        AuthResponse response = new AuthResponse();
        response.setUser(userResponse);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }
}
