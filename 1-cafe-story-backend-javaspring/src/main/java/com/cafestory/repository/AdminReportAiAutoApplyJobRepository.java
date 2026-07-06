package com.cafestory.repository;

import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
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

public interface AdminReportAiAutoApplyJobRepository extends JpaRepository<AdminReportAiAutoApplyJob, UUID> {

    Page<AdminReportAiAutoApplyJob> findByContentReportId(UUID contentReportId, Pageable pageable);

    Optional<AdminReportAiAutoApplyJob> findFirstByContentReportIdAndStatusInOrderByCreatedAtDesc(
            UUID contentReportId,
            Collection<AdminReportAiAutoApplyJobStatus> statuses);

    @Query(
            value = """
                    select *
                    from admin_report_ai_auto_apply_jobs
                    where status = :scheduledStatus
                      and scheduled_at <= :now
                    order by scheduled_at asc, created_at asc
                    limit :limit
                    for update skip locked
                    """,
            nativeQuery = true)
    List<AdminReportAiAutoApplyJob> claimDueJobs(
            @Param("scheduledStatus") String scheduledStatus,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit);
}
