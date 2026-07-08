package com.cafestory.entity;

import com.cafestory.entity.enums.AdminAssistantDraftActionStatus;
import com.cafestory.entity.enums.AdminAssistantDraftActionType;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "admin_assistant_draft_actions",
        indexes = {
                @Index(name = "idx_admin_assistant_draft_actions_conversation_created", columnList = "conversation_id, created_at"),
                @Index(name = "idx_admin_assistant_draft_actions_status_expires", columnList = "status, expires_at")
        })
public class AdminAssistantDraftAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AdminAssistantConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private AdminAssistantMessage message;

    @NotNull
    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private AdminAssistantDraftActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_refs", columnDefinition = "jsonb")
    private List<Map<String, Object>> sourceRefs;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private AdminAssistantDraftActionStatus status = AdminAssistantDraftActionStatus.PENDING;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by_admin_user_id")
    private UUID executedByAdminUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_result", columnDefinition = "jsonb")
    private Map<String, Object> executionResult;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = AdminAssistantDraftActionStatus.PENDING;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
