package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagSnapshotItemResponseDTO {

    private String sourceType;
    private String sourceId;
    private LocalDateTime updatedAt;
    private Map<String, Object> data;
}
