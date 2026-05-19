package com.cafestory.entity;

import com.cafestory.entity.enums.PageStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "cafe_pages")
public class CafePage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "region_id")
    private UUID regionId;

    @NotBlank(message = "Cafe page name is mandatory")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "Cafe page address is mandatory")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PageStatus status = PageStatus.DRAFT;

    @Column(name = "like_count", nullable = false, columnDefinition = "integer default 0")
    private Integer likeCount = 0;

    @Column(name = "follower_count", nullable = false, columnDefinition = "integer default 0")
    private Integer followerCount = 0;

    @OneToMany(mappedBy = "cafePage")
    private List<PageMember> members = new ArrayList<>();

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = PageStatus.DRAFT;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
        if (followerCount == null) {
            followerCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
