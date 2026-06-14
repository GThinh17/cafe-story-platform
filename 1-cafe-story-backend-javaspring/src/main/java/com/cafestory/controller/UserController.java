package com.cafestory.controller;

import com.cafestory.dto.requestDTO.RegionRequestDTO;
import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.service.serviceInterface.AvatarStorageService;
import com.cafestory.service.serviceInterface.UserService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AvatarStorageService avatarStorageService;

    public UserController(UserService userService, AvatarStorageService avatarStorageService) {
        this.userService = userService;
        this.avatarStorageService = avatarStorageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return userService.createUser(userCreateDTO);
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.getAllUsers(optionalUserId(principal));
    }

    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        UUID currentUserId = requireUserId(principal);
        return userService.getUserById(currentUserId, currentUserId);
    }

    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.getUserById(userId, optionalUserId(principal));
    }

    @GetMapping("/by-username/{username}")
    public UserResponseDTO getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.getUserByUsername(username, optionalUserId(principal));
    }

    @PatchMapping("/me")
    public UserResponseDTO updateUser(
            @Valid @RequestBody UserUpdateDTO userUpdateDTO,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return userService.updateUser(requireUserId(principal), userUpdateDTO);
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO updateCurrentUserAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        String publicBaseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserAvatar(avatarStorageService.storeAvatar(avatarFile, publicBaseUrl));

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

    private UUID optionalUserId(AuthenticatedUserPrincipal principal) {
        return principal == null ? null : principal.userId();
    }
}
