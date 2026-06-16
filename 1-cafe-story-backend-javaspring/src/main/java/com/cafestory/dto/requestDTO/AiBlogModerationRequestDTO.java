package com.cafestory.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiBlogModerationRequestDTO {

    private UUID blogId;

    private String caption;

    private List<String> imageUrls;
}
