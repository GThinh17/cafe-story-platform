package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PageMemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PageMemberStatusUpdateDTO {

    private UUID actorUserId;

    @NotNull(message = "Status is mandatory")
    private PageMemberStatus status;
}
