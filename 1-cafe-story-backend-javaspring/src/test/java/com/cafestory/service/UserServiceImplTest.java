package com.cafestory.service;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.UserMapper;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.UserProfileCacheService;
import com.cafestory.service.serviceImplement.UserServiceImpl;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionService regionService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileCacheService userProfileCacheService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_success_TC001() {
        UserCreateDTO request = createUserRequest();
        User user = user();
        User savedUser = user();
        UserResponseDTO response = userResponse(savedUser.getUserId());
        Region region = region();
        request.setRegionId(region.getRegionId());

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.empty());
        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getUserPassword())).thenReturn("encoded-password");
        when(regionService.resolveExistingRegion(region.getRegionId(), RegionRequirement.FULL_ADDRESS))
                .thenReturn(region);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponseDTO(savedUser)).thenReturn(response);

        UserResponseDTO result = userService.createUser(request);

        assertThat(result).isEqualTo(response);
        assertThat(user.getUserPassword()).isEqualTo("encoded-password");
        assertThat(user.getRegion()).isEqualTo(region);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_fail_duplicateUsername_TC002() {
        UserCreateDTO request = createUserRequest();
        User existingUser = user();

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_fail_duplicateEmail_TC003() {
        UserCreateDTO request = createUserRequest();
        User existingUser = user();

        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_success_TC004() {
        User user = user();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);
        when(userProfileCacheService.getFollowingCount(user.getUserId())).thenReturn(2);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).containsExactly(response);
        assertThat(result.get(0).getFollowingCount()).isEqualTo(2);
    }

    @Test
    void getUserById_success_TC005() {
        User user = user();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);
        when(userProfileCacheService.getFollowingCount(user.getUserId())).thenReturn(3);

        UserResponseDTO result = userService.getUserById(user.getUserId());

        assertThat(result).isEqualTo(response);
        assertThat(result.getFollowingCount()).isEqualTo(3);
    }

    @Test
    void getUserById_success_enrichesViewerFollowState_TC006() {
        User user = user();
        UUID viewerUserId = UUID.randomUUID();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);
        when(userProfileCacheService.getFollowingCount(user.getUserId())).thenReturn(4);
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerUserId, user.getUserId()))
                .thenReturn(true);

        UserResponseDTO result = userService.getUserById(user.getUserId(), viewerUserId);

        assertThat(result.getIsFollowing()).isTrue();
        assertThat(result.getFollowingCount()).isEqualTo(4);
    }

    @Test
    void getUserByUsername_success_enrichesFollowingCount_TC006_1() {
        User user = user();
        UUID viewerUserId = UUID.randomUUID();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);
        when(userProfileCacheService.getFollowingCount(user.getUserId())).thenReturn(5);

        UserResponseDTO result = userService.getUserByUsername(user.getUserName(), viewerUserId);

        assertThat(result.getFollowingCount()).isEqualTo(5);
    }

    @Test
    void getUserById_fail_notFound_TC007() {
        UUID userId = UUID.randomUUID();

        when(userValidator.validateUserExists(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateUser_success_updateAllFields_TC008() {
        User user = user();
        UserUpdateDTO request = updateUserRequest();
        UserResponseDTO response = userResponse(user.getUserId());
        Region region = region();
        request.setRegionId(region.getRegionId());
        response.setUserName(request.getUserName());
        response.setUserFullName(request.getUserFullName());
        response.setUserEmail(request.getUserEmail());
        response.setUserPhone(request.getUserPhone());
        response.setUserAvatar(request.getUserAvatar());
        response.setAccountStatus(true);

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.empty());
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getUserPassword())).thenReturn("encoded-updated-password");
        when(regionService.resolveExistingRegion(region.getRegionId(), RegionRequirement.FULL_ADDRESS))
                .thenReturn(region);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result.getUserName()).isEqualTo("updated_luan");
        assertThat(result.getUserFullName()).isEqualTo("Updated User");
        assertThat(result.getUserEmail()).isEqualTo("updated@example.com");
        assertThat(result.getUserPhone()).isEqualTo(123456789L);
        assertThat(result.getUserAvatar()).isEqualTo("https://example.com/updated.png");
        assertThat(result.getAccountStatus()).isTrue();
        assertThat(user.getAccountStatus()).isTrue();
        assertThat(user.getUserPassword()).isEqualTo("encoded-updated-password");
        assertThat(user.getRegion()).isEqualTo(region);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_success_sameUsernameAndEmail_TC009() {
        User user = user();
        UserUpdateDTO request = updateUserRequest();
        request.setUserName(user.getUserName());
        request.setUserEmail(user.getUserEmail());
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(user));
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.getUserPassword())).thenReturn("encoded-updated-password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_success_nullFields_TC010() {
        User user = user();
        UserUpdateDTO request = new UserUpdateDTO();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_fail_notFound_TC011() {
        UUID userId = UUID.randomUUID();
        UserUpdateDTO request = updateUserRequest();

        when(userValidator.validateUserExists(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_fail_duplicateUsername_TC012() {
        User user = user();
        User anotherUser = user();
        anotherUser.setUserId(UUID.randomUUID());
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserName("duplicate_user");

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userRepository.findByUserName(request.getUserName())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateUser(user.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_fail_duplicateEmail_TC013() {
        User user = user();
        User anotherUser = user();
        anotherUser.setUserId(UUID.randomUUID());
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserEmail("duplicate@example.com");

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        when(userRepository.findByUserEmail(request.getUserEmail())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateUser(user.getUserId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRegion_success_createRegionWhenUserHasNoRegion_TC015() {
        User user = user();
        RegionRequestDTO request = regionRequest();
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        Region savedRegion = region();
        savedRegion.setCity("Ho Chi Minh");
        savedRegion.setProvince("Ho Chi Minh");
        savedRegion.setWard("Ben Nghe");
        savedRegion.setArea("District 1");
        savedRegion.setStreet("Nguyen Hue");

        when(regionService.upsertRegion(null, request, RegionRequirement.FULL_ADDRESS)).thenReturn(savedRegion);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUserRegion(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        assertThat(user.getRegion()).isNotNull();
        assertThat(user.getRegion().getCity()).isEqualTo("Ho Chi Minh");
        assertThat(user.getRegion().getProvince()).isEqualTo("Ho Chi Minh");
        assertThat(user.getRegion().getWard()).isEqualTo("Ben Nghe");
        assertThat(user.getRegion().getArea()).isEqualTo("District 1");
        assertThat(user.getRegion().getStreet()).isEqualTo("Nguyen Hue");
        verify(regionService).upsertRegion(null, request, RegionRequirement.FULL_ADDRESS);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRegion_success_updateExistingRegionAndPreferArea_TC016() {
        User user = user();
        Region existingRegion = region();
        user.setRegion(existingRegion);
        RegionRequestDTO request = regionRequest();
        request.setDistrict("Ignored District");
        request.setArea("Area 1");
        UserResponseDTO response = userResponse(user.getUserId());

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);
        existingRegion.setArea("Area 1");
        existingRegion.setStreet("Nguyen Hue");

        when(regionService.upsertRegion(existingRegion, request, RegionRequirement.FULL_ADDRESS))
                .thenReturn(existingRegion);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUserRegion(user.getUserId(), request);

        assertThat(result).isEqualTo(response);
        assertThat(existingRegion.getArea()).isEqualTo("Area 1");
        assertThat(existingRegion.getStreet()).isEqualTo("Nguyen Hue");
        verify(regionService).upsertRegion(existingRegion, request, RegionRequirement.FULL_ADDRESS);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_success_TC013() {
        User user = user();

        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);

        userService.deleteUser(user.getUserId());

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_fail_notFound_TC014() {
        UUID userId = UUID.randomUUID();

        when(userValidator.validateUserExists(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private UserCreateDTO createUserRequest() {
        UserCreateDTO request = new UserCreateDTO();
        request.setUserName("luan123");
        request.setUserFullName("Nguyen Van Luan");
        request.setUserPassword("123456");
        request.setUserEmail("luan123@example.com");
        request.setUserPhone(987654321L);
        request.setUserAvatar("https://example.com/avatar.png");
        return request;
    }

    private UserUpdateDTO updateUserRequest() {
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserName("updated_luan");
        request.setUserFullName("Updated User");
        request.setUserPassword("updated-password");
        request.setUserEmail("updated@example.com");
        request.setUserPhone(123456789L);
        request.setUserAvatar("https://example.com/updated.png");
        request.setAccountStatus(false);
        return request;
    }

    private RegionRequestDTO regionRequest() {
        RegionRequestDTO request = new RegionRequestDTO();
        request.setCity("Ho Chi Minh");
        request.setProvince("Ho Chi Minh");
        request.setDistrict("District 1");
        request.setWard("Ben Nghe");
        request.setStreet("Nguyen Hue");
        return request;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("luan123");
        user.setUserFullName("Nguyen Van Luan");
        user.setUserPassword("123456");
        user.setUserEmail("luan123@example.com");
        user.setUserPhone(987654321L);
        user.setUserAvatar("https://example.com/avatar.png");
        user.setUserLike(0);
        user.setUserFollower(0);
        user.setAccountStatus(true);
        return user;
    }

    private UserResponseDTO userResponse(UUID userId) {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(userId);
        response.setUserName("luan123");
        response.setUserFullName("Nguyen Van Luan");
        response.setUserEmail("luan123@example.com");
        response.setUserPhone(987654321L);
        response.setUserAvatar("https://example.com/avatar.png");
        response.setUserLike(0);
        response.setUserFollower(0);
        response.setFollowingCount(0);
        response.setAccountStatus(true);
        return response;
    }

    private Region region() {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity("HCM");
        region.setProvince("HCM");
        region.setWard("Ben Nghe");
        region.setArea("D1");
        region.setStreet("Nguyen Hue");
        return region;
    }
}
