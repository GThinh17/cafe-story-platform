package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;

public interface RagSnapshotService {

    RagSnapshotResponseDTO getSnapshot(String sourceType, LocalDateTime since, int limit);

    Map<String, Object> formulaData();
}
