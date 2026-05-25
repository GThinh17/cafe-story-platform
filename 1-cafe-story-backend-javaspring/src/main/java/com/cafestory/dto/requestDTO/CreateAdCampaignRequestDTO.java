package com.cafestory.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CreateAdCampaignRequestDTO {

    @NotNull(message = "Payment id is mandatory")
    private UUID paymentId;

    @NotNull(message = "Cafe page id is mandatory")
    private UUID cafePageId;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 160, message = "Title must not exceed 160 characters")
    private String title;

    private String description;

    private String imageUrl;

    private String targetUrl;

    private Integer priority = 1;

    @Valid
    private List<AdTargetRegionRequestDTO> targetRegions = new ArrayList<>();

    private boolean activateNow;
}
