package com.cafestory.service;

import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceImplement.AdminModerationServiceImpl;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminModerationServiceImplTest {

    @Mock
    private AiModerationResultRepository moderationResultRepository;

    @Mock
    private BlogRepository blogRepository;

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
        assertThat(response.getBlogId()).isEqualTo(moderationResult.getBlog().getId());
        assertThat(response.getAuthorUserId()).isEqualTo(moderationResult.getBlog().getAuthor().getUserId());
        assertThat(response.getCaption()).isEqualTo("Cafe review content");
        assertThat(response.getCaptionScore()).isEqualTo(10);
        assertThat(response.getImageScore()).isEqualTo(90);
        assertThat(response.getTags()).containsExactly("study cafe", "brunch cafe", "garden cafe");
        assertThat(response.getAiStatus()).isEqualTo("APPROVE");
        assertThat(response.getBlogStatus()).isEqualTo(PostStatus.PUBLISHED);
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

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("tester");
        user.setUserFullName("Test User");
        return user;
    }
}
