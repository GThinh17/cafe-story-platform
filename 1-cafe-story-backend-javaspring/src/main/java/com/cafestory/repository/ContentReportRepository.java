package com.cafestory.repository;

import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {

    boolean existsByReporterUserIdAndBlogIdAndStatusIn(
            UUID reporterUserId,
            UUID blogId,
            Collection<ReportStatus> statuses);

    boolean existsByReporterUserIdAndCommentIdAndStatusIn(
            UUID reporterUserId,
            UUID commentId,
            Collection<ReportStatus> statuses);

    boolean existsByReporterUserIdAndReportedUserUserIdAndStatusIn(
            UUID reporterUserId,
            UUID reportedUserId,
            Collection<ReportStatus> statuses);

    boolean existsByReporterUserIdAndCafePageIdAndStatusIn(
            UUID reporterUserId,
            UUID cafePageId,
            Collection<ReportStatus> statuses);

    long countByReportedUserUserIdAndStatusIn(UUID reportedUserId, Collection<ReportStatus> statuses);

    long countByCafePageIdAndStatusIn(UUID cafePageId, Collection<ReportStatus> statuses);

    @Query("""
            select r.reportedUser.userId as targetId, count(r) as reportCount
            from ContentReport r
            where r.reportedUser.userId in :userIds
            and r.status in :statuses
            group by r.reportedUser.userId
            """)
    List<ReportCountRow> countByReportedUserIdsAndStatusIn(
            @Param("userIds") Collection<UUID> userIds,
            @Param("statuses") Collection<ReportStatus> statuses);

    @Query("""
            select r.cafePage.id as targetId, count(r) as reportCount
            from ContentReport r
            where r.cafePage.id in :cafePageIds
            and r.status in :statuses
            group by r.cafePage.id
            """)
    List<ReportCountRow> countByCafePageIdsAndStatusIn(
            @Param("cafePageIds") Collection<UUID> cafePageIds,
            @Param("statuses") Collection<ReportStatus> statuses);

    @Query("""
            select r
            from ContentReport r
            where (:status is null or r.status = :status)
            and (:targetType is null or r.targetType = :targetType)
            """)
    Page<ContentReport> findAdminReports(
            @Param("status") ReportStatus status,
            @Param("targetType") ReportTargetType targetType,
            Pageable pageable);

    interface ReportCountRow {
        UUID getTargetId();

        long getReportCount();
    }
}
