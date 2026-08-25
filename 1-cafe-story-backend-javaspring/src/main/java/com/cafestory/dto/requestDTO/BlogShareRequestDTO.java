package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.ShareType;
import lombok.Data;

import java.util.UUID;

@Data
public class BlogShareRequestDTO {

    private UUID userId;

    private ActorContextType actorContextType;

    private UUID actorCafePageId;

    private ShareType shareType = ShareType.PUBLIC;
}
