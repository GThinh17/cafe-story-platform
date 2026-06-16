package com.cafestory.entity;

import com.cafestory.entity.enums.PostStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "blogs",
        indexes = {
                @Index(name = "idx_blogs_author_created", columnList = "author_user_id, created_at"),
                @Index(name = "idx_blogs_status_created", columnList = "status, created_at"),
                @Index(name = "idx_blogs_page_status_created", columnList = "page_id, status, created_at"),
                @Index(name = "idx_blogs_region", columnList = "region_id")
        })
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    private CafePage page;

    @Column(name = "region_id")
    private UUID regionId;

    @NotBlank(message = "Content is mandatory")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "blog_images", joinColumns = @JoinColumn(name = "blog_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "allow_comment", nullable = false)
    private Boolean allowComment = true;

    @Column(name = "like_count", nullable = false, columnDefinition = "integer default 0")
    private Integer likeCount = 0;

    @Column(name = "share_count", nullable = false, columnDefinition = "integer default 0")
    private Integer shareCount = 0;

    @Column(name = "comment_count", nullable = false, columnDefinition = "integer default 0")
    private Integer commentCount = 0;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PostStatus.PUBLISHED;
        }
        if (allowComment == null) {
            allowComment = true;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
        if (shareCount == null) {
            shareCount = 0;
        }
        if (commentCount == null) {
            commentCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getPageId() {
        return page == null ? null : page.getId();
    }

    public void setPageId(UUID pageId) {
        if (pageId == null) {
            page = null;
            return;
        }

        CafePage cafePage = new CafePage();
        cafePage.setId(pageId);
        page = cafePage;
    }
}
