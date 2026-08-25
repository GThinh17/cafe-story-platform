package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CafePageCreateDTO {

    private UUID ownerUserId;

    private UUID regionId;

    @NotBlank(message = "Cafe page name is mandatory")
    private String name;

    @NotBlank(message = "Cafe page address is mandatory")
    private String address;

    private String description;

    private String avatarUrl;

    private String coverUrl;

    private List<UUID> coOwnerUserIds;
}
