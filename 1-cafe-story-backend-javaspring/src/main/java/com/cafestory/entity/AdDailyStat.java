package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "ad_daily_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_ad_daily_stats_campaign_date", columnNames = {"ad_campaign_id", "stat_date"}))
public class AdDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_daily_stat_id", updatable = false, nullable = false)
    private UUID adDailyStatId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_campaign_id", nullable = false)
    private AdCampaign adCampaign;

    @NotNull
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "impressions", nullable = false)
    private Integer impressions = 0;

    @Column(name = "clicks", nullable = false)
    private Integer clicks = 0;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (impressions == null) {
            impressions = 0;
        }
        if (clicks == null) {
            clicks = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
