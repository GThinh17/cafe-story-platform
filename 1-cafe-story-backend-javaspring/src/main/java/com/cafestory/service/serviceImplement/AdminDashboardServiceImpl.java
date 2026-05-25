package com.cafestory.service.serviceImplement;

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
import com.cafestory.service.serviceInterface.AdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CafePageRepository cafePageRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewerRepository reviewerRepository;
    private final AiModerationResultRepository moderationResultRepository;

    public AdminDashboardServiceImpl(
            UserRepository userRepository,
            CafePageRepository cafePageRepository,
            BlogRepository blogRepository,
            CommentRepository commentRepository,
            PaymentRepository paymentRepository,
            ReviewerRepository reviewerRepository,
            AiModerationResultRepository moderationResultRepository) {
        this.userRepository = userRepository;
        this.cafePageRepository = cafePageRepository;
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.paymentRepository = paymentRepository;
        this.reviewerRepository = reviewerRepository;
        this.moderationResultRepository = moderationResultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponseDTO getSummary() {
        AdminDashboardSummaryResponseDTO response = new AdminDashboardSummaryResponseDTO();
        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(userRepository.countByAccountStatus(true));
        response.setTotalCafePages(cafePageRepository.count());
        response.setActiveCafePages(cafePageRepository.countByStatus(PageStatus.ACTIVE));
        response.setTotalBlogs(blogRepository.count());
        response.setPublishedBlogs(blogRepository.countByStatus(PostStatus.PUBLISHED));
        response.setHiddenBlogs(blogRepository.countByStatus(PostStatus.HIDDEN));
        response.setTotalComments(commentRepository.count());
        response.setTotalPayments(paymentRepository.count());
        response.setPaidPayments(paymentRepository.countByPaymentStatus(PaymentStatus.PAID));
        response.setPendingPayments(paymentRepository.countByPaymentStatus(PaymentStatus.PENDING));
        response.setFailedPayments(paymentRepository.countByPaymentStatus(PaymentStatus.FAILED));
        response.setTotalReviewers(reviewerRepository.countByReviewerActive(true));
        response.setPendingModerationItems(moderationResultRepository.countByDecisionInAndResolvedFalse(List.of(
                ModerationDecision.NEEDS_REVIEW,
                ModerationDecision.VIOLATION)));
        return response;
    }
}
