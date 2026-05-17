package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.mapper.UserMapper;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        validateUniqueUserName(userCreateDTO.getUserName(), null);
        validateUniqueUserEmail(userCreateDTO.getUserEmail(), null);

        User user = userMapper.toUser(userCreateDTO);
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID userId) {
        return userMapper.toUserResponseDTO(findUserById(userId));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO) {
        User user = findUserById(userId);

        if (userUpdateDTO.getUserName() != null) {
            validateUniqueUserName(userUpdateDTO.getUserName(), userId);
            user.setUserName(userUpdateDTO.getUserName());
        }
        if (userUpdateDTO.getUserFullName() != null) {
            user.setUserFullName(userUpdateDTO.getUserFullName());
        }
        if (userUpdateDTO.getUserPassword() != null) {
            user.setUserPassword(userUpdateDTO.getUserPassword());
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
        if (userUpdateDTO.getAccountStatus() != null) {
            user.setAccountStatus(userUpdateDTO.getAccountStatus());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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
}
