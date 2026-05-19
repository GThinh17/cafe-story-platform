package com.cafestory.entity;

import com.cafestory.entity.enums.PageMemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "page_members")
public class PageMember {

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_CO_OWNER = "CO_OWNER";
    public static final String ROLE_MEMBER = "MEMBER";

    @EmbeddedId
    private PageMemberId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("pageId")
    @JoinColumn(name = "page_id", nullable = false)
    private CafePage cafePage;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "role_name", nullable = false, length = 40)
    private String roleName = ROLE_OWNER;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private PageMemberStatus status = PageMemberStatus.ACTIVE;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (id == null && cafePage != null && user != null) {
            id = new PageMemberId(cafePage.getId(), user.getUserId());
        }
        if (roleName == null || roleName.isBlank()) {
            roleName = ROLE_MEMBER;
        }
        if (status == null) {
            status = PageMemberStatus.ACTIVE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
