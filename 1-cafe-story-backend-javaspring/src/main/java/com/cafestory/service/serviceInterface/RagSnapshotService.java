package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.RagSnapshotResponseDTO;

import java.time.LocalDateTime;

public interface RagSnapshotService {

    RagSnapshotResponseDTO getSnapshot(String sourceType, LocalDateTime since, String cursorId, int limit);
}
