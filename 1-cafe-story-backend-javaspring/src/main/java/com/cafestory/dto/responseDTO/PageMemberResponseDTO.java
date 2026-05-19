package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.PageMemberStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PageMemberResponseDTO {
    private UUID cafePageId;
    private UUID userId;
    private String roleName;
    private PageMemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
