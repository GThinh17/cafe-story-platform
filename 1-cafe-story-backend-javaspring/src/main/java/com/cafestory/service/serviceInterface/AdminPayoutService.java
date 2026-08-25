package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.entity.enums.AdminPayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPayoutService {

    void generateMonthlyPayout(String month);

    Page<AdminPayoutResponseDTO> getPayouts(String month, AdminPayoutStatus status, Pageable pageable);

    AdminPayoutResponseDTO updatePayoutStatus(UUID payoutId, UUID adminUserId, AdminPayoutStatusRequest request);

    /**
     * Stripe báo transfer hỏng sau khi ta đã đánh dấu PAID: đưa payout về
     * APPROVED để admin xử lý lại, xoá dấu vết transfer và ghi lý do vào note.
     *
     * <p>Đường riêng cho webhook, không đi qua validateStatusTransition —
     * PAID -> APPROVED vẫn phải bị cấm với thao tác tay của admin.
     */
    void markTransferFailed(String stripeTransferId, String reason);
}
