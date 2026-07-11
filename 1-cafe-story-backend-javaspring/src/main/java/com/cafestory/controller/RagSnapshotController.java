package com.cafestory.controller;

import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;
import com.cafestory.service.serviceInterface.RagSnapshotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/rag")
public class RagSnapshotController {

    private final RagSnapshotService ragSnapshotService;

    public RagSnapshotController(RagSnapshotService ragSnapshotService) {
        this.ragSnapshotService = ragSnapshotService;
    }

    @GetMapping("/snapshot")
    public RagSnapshotResponseDTO getSnapshot(
            @RequestParam("sourceType") String sourceType,
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(value = "limit", required = false, defaultValue = "200") int limit) {
        return ragSnapshotService.getSnapshot(sourceType, since, limit);
    }

    // Snapshot single-object cho formula động: đơn giá + hệ số badge + ngưỡng điểm.
    // Trả về active formula hiện tại; nếu không có → {empty: true}.
    @GetMapping("/snapshots/formula")
    public Map<String, Object> getFormulaSnapshot() {
        return ragSnapshotService.formulaData();
    }
}
