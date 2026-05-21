package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ShareType;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogShareRequestDTO {

    private UUID userId;

    private ShareType shareType = ShareType.PUBLIC;
}
