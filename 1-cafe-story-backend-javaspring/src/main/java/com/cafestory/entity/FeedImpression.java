package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
        name = "feed_impressions",
        indexes = {
                @Index(name = "idx_feed_impressions_user_blog_shown", columnList = "user_id, blog_id, shown_at"),
                @Index(name = "idx_feed_impressions_user_shown", columnList = "user_id, shown_at")
        })
public class FeedImpression {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "position")
    private Integer position;

    @NotNull
    @Column(name = "shown_at", nullable = false, updatable = false)
    private LocalDateTime shownAt;

    @NotNull
    @Column(name = "clicked", nullable = false)
    private Boolean clicked = false;

    @NotNull
    @Column(name = "dismissed", nullable = false)
    private Boolean dismissed = false;

    @PrePersist
    void prePersist() {
        shownAt = LocalDateTime.now();
        if (clicked == null) {
            clicked = false;
        }
        if (dismissed == null) {
            dismissed = false;
        }
    }
}
