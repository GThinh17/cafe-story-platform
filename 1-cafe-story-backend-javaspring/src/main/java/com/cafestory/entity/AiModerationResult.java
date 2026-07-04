package com.cafestory.entity;

import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ModerationResolveAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "ai_moderation_results",
        indexes = {
                @Index(name = "idx_ai_moderation_blog_decision", columnList = "blog_id, decision"),
                @Index(name = "idx_ai_moderation_comment_decision", columnList = "comment_id, decision")
        })
public class AiModerationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @NotNull
    @Column(name = "score", nullable = false)
    private Double score;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private ModerationDecision decision;

    @Column(name = "labels", columnDefinition = "TEXT")
    private String labels;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "caption_score")
    private Integer captionScore;

    @Column(name = "caption_reason", columnDefinition = "TEXT")
    private String captionReason;

    @Column(name = "image_score")
    private Integer imageScore;

    @Column(name = "image_reason", columnDefinition = "TEXT")
    private String imageReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    @Column(name = "ai_status")
    private String aiStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response", columnDefinition = "jsonb")
    private Map<String, Object> rawResponse;

    @Column(name = "model_name")
    private String modelName;

    @NotNull
    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolved_action")
    private ModerationResolveAction resolvedAction;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (resolved == null) {
            resolved = false;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
