package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagTopReviewerResponseDTO {

    private String reviewerId;
    private String userName;
    private String userFullName;
    private String userAvatar;
    private Integer followerCount;
}
