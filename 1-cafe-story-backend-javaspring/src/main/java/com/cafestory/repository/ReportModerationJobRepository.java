package com.cafestory.repository;

import com.cafestory.entity.ReportModerationJob;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportModerationJobRepository extends JpaRepository<ReportModerationJob, UUID> {

    boolean existsByContentReportId(UUID contentReportId);

    Optional<ReportModerationJob> findByContentReportId(UUID contentReportId);

    Page<ReportModerationJob> findByStatus(ReportModerationJobStatus status, Pageable pageable);

    @Query(
            value = """
                    select *
                    from report_moderation_jobs
                    where (
                        status in (:retryableStatuses)
                        and next_attempt_at <= :now
                    ) or (
                        status = :processingStatus
                        and processing_started_at < :staleBefore
                    )
                    order by priority_score desc, created_at asc
                    limit :limit
                    for update skip locked
                    """,
            nativeQuery = true)
    List<ReportModerationJob> claimDueJobs(
            @Param("retryableStatuses") Collection<String> retryableStatuses,
            @Param("processingStatus") String processingStatus,
            @Param("now") LocalDateTime now,
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("limit") int limit);
}
