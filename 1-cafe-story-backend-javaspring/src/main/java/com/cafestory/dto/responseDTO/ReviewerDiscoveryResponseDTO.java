package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.ReviewerBadge;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerDiscoveryResponseDTO {
    private int rank;
    private UUID reviewerId;
    private UUID userId;
    private String userName;
    private String userFullName;
    private String avatar;
    private String city;
    private String province;
    private String ward;
    private String area;
    private String street;
    private long reviewCount;
    private long recentReviewCount;
    private long followerCount;
    private long totalLikeCount;
    private long totalCommentCount;
    private long totalShareCount;
    private long totalSaveCount;
    private long recentLikeCount;
    private long recentCommentCount;
    private long recentShareCount;
    private long recentSaveCount;
    private ReviewerBadge badge;
    private int badgeLevel;
    private double badgeScore;
    private double rankingScore;
    private boolean isFollowing;
    private boolean isMe;
}
