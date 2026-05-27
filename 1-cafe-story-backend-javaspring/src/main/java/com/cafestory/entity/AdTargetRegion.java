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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "ad_target_regions")
public class AdTargetRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_target_region_id", updatable = false, nullable = false)
    private UUID adTargetRegionId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_campaign_id", nullable = false)
    private AdCampaign adCampaign;

    @Column(name = "province", length = 120)
    private String province;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "area", length = 120)
    private String area;

    @Column(name = "ward", length = 120)
    private String ward;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
