package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerRankingResponseDTO {

    private int rank;

    private UUID reviewerId;

    /** Định danh chủ tài khoản — client dùng để nhận diện, không dùng để điều hướng. */
    private UUID userId;

    /** Slug của trang cá nhân: web điều hướng tới /{userName}. */
    private String userName;

    private String userAvatar;

    private long score;

    private long likeCount;

    private long shareCount;

    private long commentCount;

    private String location;
}
