package com.cafestory.dto.requestDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = 1, max = 160, message = "Title must contain between 1 and 160 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @Pattern(regexp = "(?i)^https?://\\S+$", message = "Image URL must use HTTP or HTTPS")
    private String imageUrl;

    @Size(max = 2048, message = "Target URL must not exceed 2048 characters")
    @Pattern(regexp = "(?i)^https?://\\S+$", message = "Target URL must use HTTP or HTTPS")
    private String targetUrl;

    @Valid
    private List<AdTargetRegionRequestDTO> targetRegions = new ArrayList<>();

    private boolean activateNow;
}
