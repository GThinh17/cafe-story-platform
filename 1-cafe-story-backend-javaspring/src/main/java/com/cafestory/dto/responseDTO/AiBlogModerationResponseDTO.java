package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AiBlogModerationResponseDTO {

    private UUID blogId;

    private Integer captionScore;

    private String captionReason;

    private Integer imageScore;

    private String imageReason;

    private List<String> tags;

    private String status;

    /**
     * Bản ghi này do backend tự dựng khi không gọi được service AI, không phải
     * do AI trả về. Dùng để chặn chi tiết lỗi nội bộ lọt vào thông báo gửi tác
     * giả, trong khi vẫn giữ nguyên chi tiết đó ở bản ghi lưu DB cho admin.
     */
    private boolean fallback;
}
