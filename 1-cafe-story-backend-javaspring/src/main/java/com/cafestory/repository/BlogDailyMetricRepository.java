package com.cafestory.repository;

import com.cafestory.entity.BlogDailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface BlogDailyMetricRepository extends JpaRepository<BlogDailyMetric, UUID> {
    Optional<BlogDailyMetric> findByBlogIdAndMetricDate(UUID blogId, LocalDate metricDate);
}
