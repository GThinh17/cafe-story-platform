package com.cafestory.repository;

import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.enums.BlogEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BlogEventRepository extends JpaRepository<BlogEvent, UUID> {
    long countByBlogIdAndEventTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            UUID blogId,
            BlogEventType eventType,
            LocalDateTime startAt,
            LocalDateTime endAt);

    List<BlogEvent> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startAt, LocalDateTime endAt);
}
