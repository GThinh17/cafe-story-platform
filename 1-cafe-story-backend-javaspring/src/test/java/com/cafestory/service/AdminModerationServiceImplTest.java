package com.cafestory.service;

import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.dto.responseDTO.ReportModerationJobResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationResolveAction;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ReportModerationJobRepository;
import com.cafestory.service.serviceImplement.AdminModerationServiceImpl;
import com.cafestory.service.serviceInterface.ReportModerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceImplTest {

    @Mock
    private AiModerationResultRepository moderationResultRepository;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportModerationJobRepository reportModerationJobRepository;

    @Mock
    private ReportModerationService reportModerationService;

    @InjectMocks
    private AdminModerationServiceImpl adminModerationService;

    @Test
    void getAllResults_success_mapsAiBlogFields_TC001() {
        AiModerationResult moderationResult = moderationResult();
        PageRequest pageable = PageRequest.of(0, 20);

        when(moderationResultRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(moderationResult)));

        Page<AdminModerationResultResponseDTO> result =
                adminModerationService.getAllResults(null, null, null, pageable);

        AdminModerationResultResponseDTO response = result.getContent().get(0);
        assertThat(response.getTargetType()).isEqualTo(ReportTargetType.BLOG);
        assertThat(response.getBlogId()).isEqualTo(moderationResult.getBlog().getId());
        assertThat(response.getAuthorUserId()).isEqualTo(moderationResult.getBlog().getAuthor().getUserId());
        assertThat(response.getCaption()).isEqualTo("Cafe review content");
        assertThat(response.getCaptionScore()).isEqualTo(10);
        assertThat(response.getImageScore()).isEqualTo(90);
        assertThat(response.getTags()).containsExactly("study cafe", "brunch cafe", "garden cafe");
        assertThat(response.getAiStatus()).isEqualTo("APPROVE");
        assertThat(response.getBlogStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void resolveResult_success_commentModerationUpdatesCommentStatus_TC002() {
        UUID resultId = UUID.randomUUID();
        AiModerationResult moderationResult = moderationResult();
        Comment comment = comment();
        moderationResult.setComment(comment);
        moderationResult.setBlog(comment.getBlog());
        AdminModerationResolveRequestDTO request = new AdminModerationResolveRequestDTO();
        request.setAction(ModerationResolveAction.HIDE);

        when(moderationResultRepository.findById(resultId)).thenReturn(Optional.of(moderationResult));
        when(moderationResultRepository.save(moderationResult)).thenReturn(moderationResult);

        AdminModerationResultResponseDTO response = adminModerationService.resolveResult(resultId, request);

        assertThat(comment.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(response.getTargetType()).isEqualTo(ReportTargetType.COMMENT);
        assertThat(response.getCommentId()).isEqualTo(comment.getId());
        assertThat(response.getCommentStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(response.getDecision()).isEqualTo(ModerationDecision.VIOLATION);
        verify(commentRepository).save(comment);
    }

    @Test
    void getJobs_success_delegatesToReportModerationService_TC003() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<ReportModerationJobResponseDTO> expected = new PageImpl<>(List.of(new ReportModerationJobResponseDTO()));

        when(reportModerationService.getJobs(ReportModerationJobStatus.FAILED, pageable)).thenReturn(expected);

        Page<ReportModerationJobResponseDTO> result =
                adminModerationService.getJobs(ReportModerationJobStatus.FAILED, pageable);

        assertThat(result).isEqualTo(expected);
        verify(reportModerationService).getJobs(ReportModerationJobStatus.FAILED, pageable);
    }

    @Test
    void retryReport_success_delegatesToReportModerationService_TC004() {
        UUID reportId = UUID.randomUUID();
        ReportModerationJobResponseDTO expected = new ReportModerationJobResponseDTO();

        when(reportModerationService.retryReport(reportId)).thenReturn(expected);

        ReportModerationJobResponseDTO result = adminModerationService.retryReport(reportId);

        assertThat(result).isEqualTo(expected);
        verify(reportModerationService).retryReport(reportId);
    }

    @Test
    void getAllResults_success_appliesEveryFilterPredicate_TC005() {
        AiModerationResult moderationResult = moderationResult();
        PageRequest pageable = PageRequest.of(0, 20);
        when(moderationResultRepository.findAll(any(Specification.class), eq(pageable)))
                .thenAnswer(invocation -> {
                    Specification<AiModerationResult> spec = invocation.getArgument(0);
                    // Chạy thật specification để nhánh dựng predicate được thực thi.
                    assertThat(applySpecification(spec)).isNotNull();
                    return new PageImpl<>(List.of(moderationResult));
                });

        assertThat(adminModerationService.getAllResults(
                "APPROVE", ModerationDecision.SAFE, Boolean.FALSE, pageable).getContent()).hasSize(1);
    }

    @Test
    void getAllResults_success_blankStatusMeansNoPredicate_TC006() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(moderationResultRepository.findAll(any(Specification.class), eq(pageable)))
                .thenAnswer(invocation -> {
                    Specification<AiModerationResult> spec = invocation.getArgument(0);
                    assertThat(applySpecification(spec)).isNotNull();
                    return new PageImpl<>(List.of());
                });

        assertThat(adminModerationService.getAllResults("   ", null, null, pageable).getContent()).isEmpty();
    }

    @Test
    void getQueue_success_readsPendingDecisions_TC007() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(moderationResultRepository.findByDecisionInAndResolvedFalse(any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(moderationResult())));

        assertThat(adminModerationService.getQueue(pageable).getContent()).hasSize(1);
    }

    @Test
    void getResult_success_returnsSingleResult_TC008() {
        AiModerationResult moderationResult = moderationResult();
        when(moderationResultRepository.findById(moderationResult.getId()))
                .thenReturn(Optional.of(moderationResult));

        assertThat(adminModerationService.getResult(moderationResult.getId()).getBlogId())
                .isEqualTo(moderationResult.getBlog().getId());
    }

    @Test
    void getResult_fail_notFound_TC009() {
        UUID resultId = UUID.randomUUID();
        when(moderationResultRepository.findById(resultId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminModerationService.getResult(resultId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Moderation result not found");
    }

    @Test
    void resolveResult_success_approveBlogRestoresPublished_TC010() {
        UUID resultId = UUID.randomUUID();
        AiModerationResult moderationResult = moderationResult();
        moderationResult.getBlog().setStatus(PostStatus.HIDDEN);
        AdminModerationResolveRequestDTO request = new AdminModerationResolveRequestDTO();
        request.setAction(ModerationResolveAction.APPROVE);
        when(moderationResultRepository.findById(resultId)).thenReturn(Optional.of(moderationResult));
        when(moderationResultRepository.save(moderationResult)).thenReturn(moderationResult);

        AdminModerationResultResponseDTO response = adminModerationService.resolveResult(resultId, request);

        assertThat(moderationResult.getBlog().getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.getDecision()).isEqualTo(ModerationDecision.SAFE);
        assertThat(moderationResult.getResolved()).isTrue();
        assertThat(moderationResult.getResolvedAt()).isNotNull();
        verify(blogRepository).save(moderationResult.getBlog());
    }

    @Test
    void resolveResult_success_removeActionMapsToRemovedStatus_TC011() {
        UUID resultId = UUID.randomUUID();
        AiModerationResult moderationResult = moderationResult();
        AdminModerationResolveRequestDTO request = new AdminModerationResolveRequestDTO();
        request.setAction(ModerationResolveAction.REMOVE);
        when(moderationResultRepository.findById(resultId)).thenReturn(Optional.of(moderationResult));
        when(moderationResultRepository.save(moderationResult)).thenReturn(moderationResult);

        adminModerationService.resolveResult(resultId, request);

        assertThat(moderationResult.getBlog().getStatus()).isEqualTo(PostStatus.REMOVED);
    }

    @Test
    void resolveResult_fail_resultWithoutTargetContent_TC012() {
        UUID resultId = UUID.randomUUID();
        AiModerationResult moderationResult = moderationResult();
        moderationResult.setBlog(null);
        AdminModerationResolveRequestDTO request = new AdminModerationResolveRequestDTO();
        request.setAction(ModerationResolveAction.HIDE);
        when(moderationResultRepository.findById(resultId)).thenReturn(Optional.of(moderationResult));

        assertThatThrownBy(() -> adminModerationService.resolveResult(resultId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Moderation result has no target content");
    }

    private jakarta.persistence.criteria.Predicate applySpecification(
            Specification<AiModerationResult> specification) {
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.Root<AiModerationResult> root =
                org.mockito.Mockito.mock(jakarta.persistence.criteria.Root.class);
        @SuppressWarnings("unchecked")
        jakarta.persistence.criteria.CriteriaQuery<AiModerationResult> query =
                org.mockito.Mockito.mock(jakarta.persistence.criteria.CriteriaQuery.class);
        jakarta.persistence.criteria.CriteriaBuilder builder =
                org.mockito.Mockito.mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        jakarta.persistence.criteria.Predicate predicate =
                org.mockito.Mockito.mock(jakarta.persistence.criteria.Predicate.class);
        org.mockito.Mockito.lenient().when(builder.equal(any(), any(Object.class))).thenReturn(predicate);
        org.mockito.Mockito.lenient().when(builder.conjunction()).thenReturn(predicate);
        org.mockito.Mockito.lenient()
                .when(builder.and(any(jakarta.persistence.criteria.Predicate[].class))).thenReturn(predicate);
        return specification.toPredicate(root, query, builder);
    }

    private AiModerationResult moderationResult() {
        AiModerationResult result = new AiModerationResult();
        result.setId(UUID.randomUUID());
        result.setBlog(blog());
        result.setCaption("Cafe review content");
        result.setScore(10.0);
        result.setDecision(ModerationDecision.SAFE);
        result.setCaptionScore(10);
        result.setCaptionReason("Caption ok");
        result.setImageScore(90);
        result.setImageReason("Image ok");
        result.setTags(List.of("study cafe", "brunch cafe", "garden cafe"));
        result.setAiStatus("APPROVE");
        return result;
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(user());
        blog.setStatus(PostStatus.PUBLISHED);
        return blog;
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(blog());
        comment.setUser(user());
        comment.setContent("Unsafe comment");
        comment.setStatus(PostStatus.PUBLISHED);
        return comment;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("tester");
        user.setUserFullName("Test User");
        return user;
    }
}
