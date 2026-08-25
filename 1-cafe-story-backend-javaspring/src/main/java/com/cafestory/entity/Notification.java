package com.cafestory.entity;

import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.FollowTargetType;
import com.cafestory.entity.enums.NotificationType;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_context_type", nullable = false)
    private ActorContextType actorContextType = ActorContextType.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_cafe_page_id")
    private CafePage actorCafePage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "blog_id")
    private UUID blogId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private FollowTargetType targetType;

    @Column(name = "target_cafe_page_id")
    private UUID targetCafePageId;

    @Column(name = "comment_id")
    private UUID commentId;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "moderation_status")
    private String moderationStatus;

    @Column(name = "moderation_reason", length = 1024)
    private String moderationReason;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isRead == null) {
            isRead = false;
        }
        if (actorContextType == null) {
            actorContextType = ActorContextType.USER;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
