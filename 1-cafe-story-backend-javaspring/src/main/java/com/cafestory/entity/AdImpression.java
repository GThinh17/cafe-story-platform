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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ad_impressions")
public class AdImpression {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_impression_id", updatable = false, nullable = false)
    private UUID adImpressionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_campaign_id", nullable = false)
    private AdCampaign adCampaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "shown_at", nullable = false)
    private LocalDateTime shownAt;

    @PrePersist
    void prePersist() {
        if (shownAt == null) {
            shownAt = LocalDateTime.now();
        }
    }
}
