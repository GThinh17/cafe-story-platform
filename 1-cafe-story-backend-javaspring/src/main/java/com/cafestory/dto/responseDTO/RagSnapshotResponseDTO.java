package com.cafestory.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagSnapshotResponseDTO {

    private List<RagSnapshotItemResponseDTO> items;
    private List<String> tombstones;
    private LocalDateTime nextSince;
    private String nextSourceId;
    private boolean hasMore;
}
