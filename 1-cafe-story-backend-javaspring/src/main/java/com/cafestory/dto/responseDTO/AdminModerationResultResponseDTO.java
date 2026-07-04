package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ModerationResolveAction;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AdminModerationResultResponseDTO {

    private UUID id;

    private UUID blogId;

    private UUID commentId;

    private ReportTargetType targetType;

    private UUID authorUserId;

    private String authorUserName;

    private String authorUserFullName;

    private String authorUserAvatar;

    private String caption;

    private Double score;

    private ModerationDecision decision;

    private Integer captionScore;

    private String captionReason;

    private Integer imageScore;

    private String imageReason;

    private List<String> tags;

    private String aiStatus;

    private PostStatus blogStatus;

    private PostStatus commentStatus;

    private String labels;

    private String explanation;

    private String modelName;

    private Boolean resolved;

    private ModerationResolveAction resolvedAction;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
