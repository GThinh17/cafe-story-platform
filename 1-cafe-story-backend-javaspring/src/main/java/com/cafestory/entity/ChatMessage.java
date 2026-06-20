package com.cafestory.entity;

import com.cafestory.entity.enums.MessageStatus;
import com.cafestory.entity.enums.MessageType;
import com.cafestory.entity.enums.ChatSenderContextType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_context_type", nullable = false, length = 40)
    private ChatSenderContextType senderContextType = ChatSenderContextType.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_cafe_page_id")
    private CafePage senderCafePage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MessageType type;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @ElementCollection
    @CollectionTable(name = "chat_message_images", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "sticker_url")
    private String stickerUrl;

    @Column(name = "sticker_id")
    private String stickerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    @NotNull
    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

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
            status = MessageStatus.SENT;
        }
        if (senderContextType == null) {
            senderContextType = ChatSenderContextType.USER;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
