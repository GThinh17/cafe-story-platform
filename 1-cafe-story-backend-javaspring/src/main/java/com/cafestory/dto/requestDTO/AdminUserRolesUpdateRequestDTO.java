package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.UserRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUserRolesUpdateRequestDTO {

    @NotEmpty(message = "roles must not be empty")
    private Set<@NotNull UserRole> roles;
}
