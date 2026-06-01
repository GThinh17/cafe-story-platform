package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.*;

import com.cafestory.dto.responseDTO.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userCreateDTO);

    List<UserResponseDTO> getAllUsers();

    List<UserResponseDTO> getAllUsers(UUID viewerUserId);

    UserResponseDTO getUserById(UUID userId);

    UserResponseDTO getUserById(UUID userId, UUID viewerUserId);

    UserResponseDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO);

    UserResponseDTO updateUserRegion(UUID userId, RegionRequestDTO regionRequestDTO);

    void deleteUser(UUID userId);
}
