package com.cafestory.controller;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.service.serviceInterface.UserService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return userService.createUser(userCreateDTO);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    @PatchMapping("/me")
    public UserResponseDTO updateUser(
            @Valid @RequestBody UserUpdateDTO userUpdateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.updateUser(requireUserId(principal), userUpdateDTO);
    }

    @PatchMapping("/me/region")
    public UserResponseDTO updateUserRegion(
            @RequestBody RegionRequestDTO regionRequestDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.updateUserRegion(requireUserId(principal), regionRequestDTO);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        userService.deleteUser(requireUserId(principal));
    }
}
