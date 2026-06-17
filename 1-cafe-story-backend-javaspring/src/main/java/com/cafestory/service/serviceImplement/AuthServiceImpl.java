package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.LoginRequest;
import com.cafestory.dto.requestDTO.RegisterRequest;
import com.cafestory.dto.responseDTO.AuthResponse;
import com.cafestory.dto.responseDTO.AuthUserResponse;
import com.cafestory.dto.responseDTO.UsernameSuggestionResponse;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.RefreshToken;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import com.cafestory.repository.CafePageRepository;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MAX_USERNAME_LENGTH = 10;
    private static final int MIN_SUGGESTION_COUNT = 4;
    private static final int MAX_SUGGESTION_COUNT = 7;
    private static final int MAX_CANDIDATE_COUNT = 25;
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-z0-9](?!.*[._]{2})[a-z0-9._]{3,8}[a-z0-9]$");
    private static final Set<String> RESERVED_USER_NAMES = Set.of(
            "admin",
            "root",
            "login",
            "register",
            "api",
            "me",
            "support",
            "cafestory");

    private final UserRepository userRepository;
    private final CafePageRepository cafePageRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserValidator userValidator;

    public AuthServiceImpl(
            UserRepository userRepository,
            CafePageRepository cafePageRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserValidator userValidator) {
        this.userRepository = userRepository;
        this.cafePageRepository = cafePageRepository;
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
        user.setUserDescription(request.getUserDescription());
        user.setAccountStatus(true);

        User savedUser = userRepository.save(user);
        assignDefaultUserRole(savedUser);
        List<String> roles = roles(savedUser.getUserId());
        String accessToken = jwtService.createAccessToken(savedUser, roles);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser);
        return response(savedUser, roles, accessToken, refreshToken);
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
    @Transactional(readOnly = true)
    public UsernameSuggestionResponse suggestUserNames(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }

        List<String> candidates = generateUserNameCandidates(fullName);
        List<String> existingUserNames = userRepository.findExistingUserNamesLowercase(candidates);
        Set<String> existingUserNameSet = new LinkedHashSet<>(existingUserNames);

        List<String> suggestions = candidates.stream()
                .filter(candidate -> isValidUserName(candidate))
                .filter(candidate -> !RESERVED_USER_NAMES.contains(candidate))
                .filter(candidate -> !existingUserNameSet.contains(candidate))
                .limit(MAX_SUGGESTION_COUNT)
                .toList();

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setSuggestions(suggestions);
        return response;
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

    private List<String> generateUserNameCandidates(String fullName) {
        List<String> tokens = normalizeNameTokens(fullName);
        String first = tokens.isEmpty() ? "user" : tokens.get(0);
        String last = tokens.isEmpty() ? "user" : tokens.get(tokens.size() - 1);
        String penultimate = tokens.size() > 1 ? tokens.get(tokens.size() - 2) : "";
        String joined = String.join("", tokens);
        String condensedJoined = removeInnerVowels(joined);
        int seed = Math.floorMod(normalizeForSeed(fullName).hashCode(), Integer.MAX_VALUE);

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addCandidate(candidates, first + "." + last);
        addCandidate(candidates, first + "_" + last);
        addCandidate(candidates, first + last);
        addCandidate(candidates, last + "." + first);
        addCandidate(candidates, abbreviate(first, 4) + last);
        addCandidate(candidates, first + abbreviate(last, 4));

        if (!penultimate.isEmpty() && !penultimate.equals(first)) {
            addCandidate(candidates, penultimate + "." + last);
            addCandidate(candidates, penultimate + "_" + last);
            addCandidate(candidates, penultimate + last);
            addCandidate(candidates, first + abbreviate(penultimate, 1) + "_" + last);
            addCandidate(candidates, abbreviate(penultimate, 4) + last + suffix(seed, 2));
        }

        addCandidate(candidates, joined);
        addCandidate(candidates, condensedJoined);
        addCandidate(candidates, condensedJoined + suffix(seed, 2));
        addCandidate(candidates, abbreviate(joined, MAX_USERNAME_LENGTH));

        int suffixAttempt = 0;
        while (candidates.size() < MAX_CANDIDATE_COUNT) {
            String base = candidates.size() < MIN_SUGGESTION_COUNT ? "user" : joined;
            if (base.isBlank()) {
                base = "user";
            }
            addCandidateWithSuffix(candidates, base, suffix(seed + suffixAttempt * 31, suffixAttempt));
            suffixAttempt++;
        }

        return new ArrayList<>(candidates);
    }

    private List<String> normalizeNameTokens(String fullName) {
        String normalized = removeVietnameseDiacritics(fullName.trim().toLowerCase())
                .replaceAll("[^a-z0-9\\s._]", " ")
                .replaceAll("[._]+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return List.of("user");
        }
        return List.of(normalized.split("\\s+")).stream()
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String removeVietnameseDiacritics(String value) {
        String normalized = value.replace('đ', 'd').replace('Đ', 'D');
        return Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String normalizeForSeed(String fullName) {
        return removeVietnameseDiacritics(fullName.trim().toLowerCase());
    }

    private void addCandidate(LinkedHashSet<String> candidates, String candidate) {
        if (candidates.size() >= MAX_CANDIDATE_COUNT) {
            return;
        }

        String normalized = normalizeCandidate(candidate);
        if (!normalized.isBlank()) {
            candidates.add(normalized);
        }
    }

    private void addCandidateWithSuffix(LinkedHashSet<String> candidates, String base, String suffix) {
        int baseMaxLength = MAX_USERNAME_LENGTH - suffix.length();
        String shortenedBase = normalizeCandidate(base);
        if (shortenedBase.length() > baseMaxLength) {
            shortenedBase = shortenedBase.substring(0, baseMaxLength).replaceAll("[._]+$", "");
        }
        addCandidate(candidates, shortenedBase + suffix);
    }

    private String normalizeCandidate(String candidate) {
        String normalized = removeVietnameseDiacritics(candidate.toLowerCase())
                .replaceAll("[^a-z0-9._]", "")
                .replaceAll("[._]{2,}", "_")
                .replaceAll("^[._]+|[._]+$", "");
        if (normalized.length() > MAX_USERNAME_LENGTH) {
            normalized = normalized.substring(0, MAX_USERNAME_LENGTH).replaceAll("[._]+$", "");
        }
        return normalized;
    }

    private boolean isValidUserName(String candidate) {
        return candidate.length() >= MIN_USERNAME_LENGTH
                && candidate.length() <= MAX_USERNAME_LENGTH
                && VALID_USERNAME_PATTERN.matcher(candidate).matches();
    }

    private String suffix(int seed, int attempt) {
        int number = Math.floorMod(seed + attempt * 17, 997);
        if (number < 2) {
            number += 2;
        }
        return "_" + number;
    }

    private String abbreviate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String removeInnerVowels(String value) {
        if (value.length() <= MIN_USERNAME_LENGTH) {
            return value;
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (index == 0 || index == value.length() - 1 || "aeiou".indexOf(current) < 0) {
                builder.append(current);
            }
        }
        return builder.toString();
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

    private UUID cafePageId(UUID userId) {
        List<CafePage> pages = cafePageRepository.findByOwnerUserId(userId);
        if (pages == null) {
            return null;
        }

        return pages.stream()
                .filter(page -> page.getId() != null)
                .sorted((left, right) -> Boolean.compare(
                        Boolean.TRUE.equals(right.getPageActive()),
                        Boolean.TRUE.equals(left.getPageActive())))
                .map(CafePage::getId)
                .findFirst()
                .orElse(null);
    }

    private AuthResponse response(User user, List<String> roles, String accessToken, String refreshToken) {
        AuthUserResponse userResponse = new AuthUserResponse();
        UUID cafePageId = cafePageId(user.getUserId());
        userResponse.setUserId(user.getUserId());
        userResponse.setUserName(user.getUserName());
        userResponse.setUserFullName(user.getUserFullName());
        userResponse.setUserEmail(user.getUserEmail());
        userResponse.setUserPhone(user.getUserPhone());
        userResponse.setUserAvatar(user.getUserAvatar());
        userResponse.setUserDescription(user.getUserDescription());
        userResponse.setAccountStatus(user.getAccountStatus());
        userResponse.setCafePageId(cafePageId);
        userResponse.setPageId(cafePageId);
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
