package com.cafestory.repository;

import com.cafestory.entity.Notification;
import com.cafestory.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = {"actor", "actorCafePage", "recipient"})
    Page<Notification> findByRecipientUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"actor", "actorCafePage", "recipient"})
    Page<Notification> findByRecipientUserIdAndType(UUID userId, NotificationType type, Pageable pageable);

    @EntityGraph(attributePaths = {"actor", "actorCafePage", "recipient"})
    Page<Notification> findByRecipientUserIdAndIsRead(UUID userId, Boolean isRead, Pageable pageable);

    @EntityGraph(attributePaths = {"actor", "actorCafePage", "recipient"})
    List<Notification> findByRecipientUserIdAndIsRead(UUID userId, Boolean isRead);

    @EntityGraph(attributePaths = {"actor", "actorCafePage", "recipient"})
    Page<Notification> findByRecipientUserIdAndTypeAndIsRead(UUID userId, NotificationType type, Boolean isRead, Pageable pageable);

    long countByRecipientUserIdAndIsRead(UUID userId, Boolean isRead);
}
