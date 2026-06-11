package com.cafestory.repository;

import com.cafestory.entity.ReportReason;
import com.cafestory.entity.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportReasonRepository extends JpaRepository<ReportReason, UUID> {

    Optional<ReportReason> findByCode(String code);

    List<ReportReason> findByActiveTrueOrderBySeverityDescSortOrderAscLabelViAsc();

    @Query("""
            select r
            from ReportReason r
            where r.active = true
            and (r.targetType is null or r.targetType = :targetType)
            order by r.severity desc, r.sortOrder asc, r.labelVi asc
            """)
    List<ReportReason> findActiveReasonsForTargetType(@Param("targetType") ReportTargetType targetType);
}
