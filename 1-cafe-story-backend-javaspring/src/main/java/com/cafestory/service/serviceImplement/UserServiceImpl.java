package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.mapper.UserMapper;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.UserService;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final RegionRepository regionRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            UserFollowRepository userFollowRepository,
            RegionRepository regionRepository,
            UserMapper userMapper,
            UserValidator userValidator,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.regionRepository = regionRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        validateUniqueUserName(userCreateDTO.getUserName(), null);
        validateUniqueUserEmail(userCreateDTO.getUserEmail(), null);

        User user = userMapper.toUser(userCreateDTO);
        user.setUserPassword(passwordEncoder.encode(userCreateDTO.getUserPassword()));
        if (userCreateDTO.getRegionId() != null) {
            user.setRegion(regionRepository.findById(userCreateDTO.getRegionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found")));
        }
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return getAllUsers(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(UUID viewerUserId) {
        return userRepository.findAll()
                .stream()
                .map(user -> toUserResponseDTO(user, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        return getUserById(userId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId, UUID viewerUserId) {
        return toUserResponseDTO(userValidator.validateUserExists(userId), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username, UUID viewerUserId) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return getUserById(user.getUserId(), viewerUserId);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO) {
        User user = userValidator.validateUserExists(userId);

        if (userUpdateDTO.getUserName() != null) {
            validateUniqueUserName(userUpdateDTO.getUserName(), userId);
            user.setUserName(userUpdateDTO.getUserName());
        }
        if (userUpdateDTO.getUserFullName() != null) {
            user.setUserFullName(userUpdateDTO.getUserFullName());
        }
        if (userUpdateDTO.getUserPassword() != null) {
            user.setUserPassword(passwordEncoder.encode(userUpdateDTO.getUserPassword()));
        }
        if (userUpdateDTO.getUserEmail() != null) {
            validateUniqueUserEmail(userUpdateDTO.getUserEmail(), userId);
            user.setUserEmail(userUpdateDTO.getUserEmail());
        }
        if (userUpdateDTO.getUserPhone() != null) {
            user.setUserPhone(userUpdateDTO.getUserPhone());
        }
        if (userUpdateDTO.getUserAvatar() != null) {
            user.setUserAvatar(userUpdateDTO.getUserAvatar());
        }
        if (userUpdateDTO.getRegionId() != null) {
            user.setRegion(regionRepository.findById(userUpdateDTO.getRegionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found")));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRegion(UUID userId, RegionRequestDTO regionRequestDTO) {
        User user = userValidator.validateUserExists(userId);
        Region region = user.getRegion();
        if (region == null) {
            region = new Region();
            user.setRegion(region);
        }

        if (regionRequestDTO.getCity() != null) {
            region.setCity(regionRequestDTO.getCity());
        }
        if (regionRequestDTO.getProvince() != null) {
            region.setProvince(regionRequestDTO.getProvince());
        }
        if (regionRequestDTO.getWard() != null) {
            region.setWard(regionRequestDTO.getWard());
        }
        String area = firstNonBlank(regionRequestDTO.getArea(), regionRequestDTO.getDistrict());
        if (area != null) {
            region.setArea(area);
        }
        if (regionRequestDTO.getStreet() != null) {
            region.setStreet(regionRequestDTO.getStreet());
        }

        Region savedRegion = regionRepository.save(region);
        user.setRegion(savedRegion);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userValidator.validateUserExists(userId);
        userRepository.delete(user);
    }

    private void validateUniqueUserName(String userName, UUID currentUserId) {
        userRepository.findByUserName(userName)
                .filter(user -> currentUserId == null || !user.getUserId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                });
    }

    private void validateUniqueUserEmail(String userEmail, UUID currentUserId) {
        userRepository.findByUserEmail(userEmail)
                .filter(user -> currentUserId == null || !user.getUserId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                });
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private UserResponseDTO toUserResponseDTO(User user, UUID viewerUserId) {
        UserResponseDTO response = userMapper.toUserResponseDTO(user);
        response.setIsFollowing(viewerUserId != null
                && user.getUserId() != null
                && !viewerUserId.equals(user.getUserId())
                && userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerUserId, user.getUserId()));
        return response;
    }
}
