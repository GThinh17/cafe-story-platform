package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.ShareType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BlogShareResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID blogId;
    private ActorContextType actorContextType;
    private UUID actorCafePageId;
    private String actorDisplayName;
    private String actorAvatarUrl;
    private ShareType shareType;
    private LocalDateTime createdAt;
}
