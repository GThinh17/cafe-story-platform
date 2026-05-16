package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.*;

import com.cafestory.dto.responseDTO.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userCreateDTO);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(UUID userId);

    UserResponseDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO);

    void deleteUser(UUID userId);
}
