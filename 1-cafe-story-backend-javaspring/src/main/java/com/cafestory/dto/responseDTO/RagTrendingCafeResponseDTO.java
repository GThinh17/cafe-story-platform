package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagTrendingCafeResponseDTO {

    private String id;
    private String name;
    private String address;
    private String province;
    private String city;
    private Integer followerCount;
    private Integer likeCount;
    private String avatarUrl;
}
