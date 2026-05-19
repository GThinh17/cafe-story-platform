package com.cafestory.entity;

import com.cafestory.entity.enums.TrendWindowType;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "blog_trending_scores")
public class BlogTrendingScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "window_type", nullable = false)
    private TrendWindowType windowType;

    @NotNull
    @Column(name = "trend_score", nullable = false)
    private Double trendScore;

    @NotNull
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
