package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PageStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class CafePageUpdateDTO {
    private UUID regionId;
    private String name;
    private String address;
    private String description;
    private String avatarUrl;
    private String coverUrl;
    private PageStatus status;
}
