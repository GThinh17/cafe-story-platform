package com.cafestory.service;

import com.cafestory.dto.responseDTO.AdminDashboardSummaryResponseDTO;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.AdminDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminDashboardServiceImpl}.
 *
 * <p>Lớp này chỉ gom số đếm từ bảy repository, nên phép kiểm xác nhận từng ô
 * trên bảng điều khiển lấy đúng nguồn — nhầm nguồn là lỗi âm thầm, số vẫn hiện
 * ra bình thường nhưng sai ý nghĩa.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CafePageRepository cafePageRepository;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReviewerRepository reviewerRepository;
    @Mock
    private AiModerationResultRepository moderationResultRepository;

    private AdminDashboardServiceImpl adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardServiceImpl(
                userRepository,
                cafePageRepository,
                blogRepository,
                commentRepository,
                paymentRepository,
                reviewerRepository,
                moderationResultRepository);
    }

    @Test
    void getSummary_success_readsEveryCounterFromItsOwnSource_TC001() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByAccountStatus(true)).thenReturn(90L);
        when(cafePageRepository.count()).thenReturn(20L);
        when(cafePageRepository.countByStatus(PageStatus.ACTIVE)).thenReturn(15L);
        when(blogRepository.count()).thenReturn(500L);
        when(blogRepository.countByStatus(PostStatus.PUBLISHED)).thenReturn(450L);
        when(blogRepository.countByStatus(PostStatus.HIDDEN)).thenReturn(30L);
        when(commentRepository.count()).thenReturn(1_200L);
        when(paymentRepository.count()).thenReturn(80L);
        when(paymentRepository.countByPaymentStatus(PaymentStatus.PAID)).thenReturn(60L);
        when(paymentRepository.countByPaymentStatus(PaymentStatus.PENDING)).thenReturn(15L);
        when(paymentRepository.countByPaymentStatus(PaymentStatus.FAILED)).thenReturn(5L);
        when(reviewerRepository.countByReviewerActive(true)).thenReturn(12L);
        when(moderationResultRepository.countByDecisionInAndResolvedFalse(
                List.of(ModerationDecision.NEEDS_REVIEW, ModerationDecision.VIOLATION)))
                .thenReturn(7L);

        AdminDashboardSummaryResponseDTO result = adminDashboardService.getSummary();

        assertThat(result.getTotalUsers()).isEqualTo(100L);
        assertThat(result.getActiveUsers()).isEqualTo(90L);
        assertThat(result.getTotalCafePages()).isEqualTo(20L);
        assertThat(result.getActiveCafePages()).isEqualTo(15L);
        assertThat(result.getTotalBlogs()).isEqualTo(500L);
        assertThat(result.getPublishedBlogs()).isEqualTo(450L);
        assertThat(result.getHiddenBlogs()).isEqualTo(30L);
        assertThat(result.getTotalComments()).isEqualTo(1_200L);
        assertThat(result.getTotalPayments()).isEqualTo(80L);
        assertThat(result.getPaidPayments()).isEqualTo(60L);
        assertThat(result.getPendingPayments()).isEqualTo(15L);
        assertThat(result.getFailedPayments()).isEqualTo(5L);
        assertThat(result.getTotalReviewers()).isEqualTo(12L);
        assertThat(result.getPendingModerationItems()).isEqualTo(7L);
    }
}
