package com.cafestory.entity;

import com.cafestory.entity.enums.AdStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "ad_campaigns")
public class AdCampaign {

    public static final int DEFAULT_MAX_IMPRESSIONS = 10000;
    public static final int DEFAULT_MAX_DURATION_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_campaign_id", updatable = false, nullable = false)
    private UUID adCampaignId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_page_id", nullable = false)
    private CafePage cafePage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    @NotNull
    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "target_url", columnDefinition = "TEXT")
    private String targetUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdStatus status = AdStatus.DRAFT;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

    @Column(name = "max_impressions", nullable = false)
    private Integer maxImpressions = DEFAULT_MAX_IMPRESSIONS;

    @Column(name = "served_impressions", nullable = false)
    private Integer servedImpressions = 0;

    @Column(name = "max_duration_days", nullable = false)
    private Integer maxDurationDays = DEFAULT_MAX_DURATION_DAYS;

    @OneToMany(mappedBy = "adCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdTargetRegion> targetRegions = new ArrayList<>();

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        applyDefaults();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (status == null) {
            status = AdStatus.DRAFT;
        }
        if (priority == null) {
            priority = 1;
        }
        if (maxImpressions == null) {
            maxImpressions = DEFAULT_MAX_IMPRESSIONS;
        }
        if (servedImpressions == null) {
            servedImpressions = 0;
        }
        if (maxDurationDays == null) {
            maxDurationDays = DEFAULT_MAX_DURATION_DAYS;
        }
    }
}
