package com.cafestory.service;

import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceImplement.BlogModerationTagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogModerationTagServiceImplTest {

    @Mock
    private AiModerationResultRepository moderationResultRepository;

    private BlogModerationTagServiceImpl blogModerationTagService;

    @BeforeEach
    void setUp() {
        blogModerationTagService = new BlogModerationTagServiceImpl(moderationResultRepository);
    }

    @Test
    void getTagsByBlogIds_emptyInput_skipsRepository_TC001() {
        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(List.of());

        assertThat(tags).isEmpty();
        verify(moderationResultRepository, never()).findWithTagsByBlogIds(anyCollection());
    }

    @Test
    void getTagsByBlogIds_nullInput_skipsRepository_TC002() {
        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(null);

        assertThat(tags).isEmpty();
        verify(moderationResultRepository, never()).findWithTagsByBlogIds(anyCollection());
    }

    @Test
    void getTagsByBlogIds_batchLoadsOnce_TC003() {
        UUID firstBlogId = UUID.randomUUID();
        UUID secondBlogId = UUID.randomUUID();
        when(moderationResultRepository.findWithTagsByBlogIds(anyCollection())).thenReturn(List.of(
                moderationResult(firstBlogId, List.of("study cafe", "garden cafe")),
                moderationResult(secondBlogId, List.of("pet cafe"))));

        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(
                List.of(firstBlogId, secondBlogId));

        assertThat(tags).containsOnlyKeys(firstBlogId, secondBlogId);
        assertThat(tags.get(firstBlogId)).containsExactly("study cafe", "garden cafe");
        assertThat(tags.get(secondBlogId)).containsExactly("pet cafe");
        verify(moderationResultRepository).findWithTagsByBlogIds(anyCollection());
    }

    @Test
    void getTagsByBlogIds_keepsLatestModerationRow_TC004() {
        UUID blogId = UUID.randomUUID();
        // Repository order by blogId, createdAt desc nên row đầu tiên là lần moderate mới nhất.
        when(moderationResultRepository.findWithTagsByBlogIds(anyCollection())).thenReturn(List.of(
                moderationResult(blogId, List.of("brunch cafe")),
                moderationResult(blogId, List.of("vintage cafe"))));

        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(List.of(blogId));

        assertThat(tags.get(blogId)).containsExactly("brunch cafe");
    }

    @Test
    void getTagsByBlogIds_dropsBlankAndDuplicateTags_TC005() {
        UUID blogId = UUID.randomUUID();
        when(moderationResultRepository.findWithTagsByBlogIds(anyCollection())).thenReturn(List.of(
                moderationResult(blogId, Arrays.asList("  study cafe  ", null, "   ", "study cafe", "takeaway"))));

        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(List.of(blogId));

        assertThat(tags.get(blogId)).containsExactly("study cafe", "takeaway");
    }

    @Test
    void getTagsByBlogIds_omitsBlogWithoutUsableTags_TC006() {
        UUID blogId = UUID.randomUUID();
        when(moderationResultRepository.findWithTagsByBlogIds(anyCollection())).thenReturn(List.of(
                moderationResult(blogId, List.of())));

        Map<UUID, List<String>> tags = blogModerationTagService.getTagsByBlogIds(List.of(blogId));

        assertThat(tags).doesNotContainKey(blogId);
    }

    private AiModerationResult moderationResult(UUID blogId, List<String> tags) {
        Blog blog = new Blog();
        blog.setId(blogId);
        AiModerationResult result = new AiModerationResult();
        result.setBlog(blog);
        result.setTags(tags);
        return result;
    }
}
