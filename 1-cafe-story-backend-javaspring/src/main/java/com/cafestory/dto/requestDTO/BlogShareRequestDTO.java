package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ShareType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogShareRequestDTO {

    @NotNull(message = "User id is mandatory")
    private UUID userId;

    private ShareType shareType = ShareType.PUBLIC;
}
