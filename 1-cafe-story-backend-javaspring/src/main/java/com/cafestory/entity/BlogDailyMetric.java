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
        name = "blog_daily_metrics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blog_id", "metric_date"}))
public class BlogDailyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotNull
    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "views", nullable = false)
    private Long views = 0L;

    @Column(name = "likes", nullable = false)
    private Long likes = 0L;

    @Column(name = "comments", nullable = false)
    private Long comments = 0L;

    @Column(name = "shares", nullable = false)
    private Long shares = 0L;

    @Column(name = "saves", nullable = false)
    private Long saves = 0L;

    @Column(name = "reports", nullable = false)
    private Long reports = 0L;

    @Column(name = "avg_read_seconds", nullable = false)
    private Double avgReadSeconds = 0.0;

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
