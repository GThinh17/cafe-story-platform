package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminPostStatusUpdateRequestDTO {

    @NotNull(message = "status is required")
    private PostStatus status;

    /** Lý do gửi kèm thông báo cho tác giả khi từ chối. Không bắt buộc. */
    private String reason;
}
