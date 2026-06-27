package com.cafestory.service;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.requestDTO.FeedImpressionItemRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.FeedImpression;
import com.cafestory.entity.User;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.FeedImpressionRepository;
import com.cafestory.service.serviceImplement.FeedImpressionServiceImpl;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedImpressionServiceImplTest {

    @Mock
    private FeedImpressionRepository feedImpressionRepository;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserValidator userValidator;

    @Test
    void recordImpressions_success_savesAllItems_TC001() {
        UUID userId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User user = user(userId);
        Blog blog = blog(blogId);
        FeedImpressionBatchRequestDTO request = request(blogId, 1);
        FeedImpressionServiceImpl service = service();

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogRepository.findAllById(List.of(blogId))).thenReturn(List.of(blog));

        FeedImpressionResponseDTO result = service.recordImpressions(userId, request);

        assertThat(result.getRecordedCount()).isEqualTo(1);
        ArgumentCaptor<List<FeedImpression>> impressionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(feedImpressionRepository).saveAll(impressionsCaptor.capture());
        assertThat(impressionsCaptor.getValue()).hasSize(1);
        assertThat(impressionsCaptor.getValue().getFirst().getUser()).isEqualTo(user);
        assertThat(impressionsCaptor.getValue().getFirst().getBlog()).isEqualTo(blog);
        assertThat(impressionsCaptor.getValue().getFirst().getPosition()).isEqualTo(1);
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void recordImpressions_fail_blogNotFound_TC002() {
        UUID userId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User user = user(userId);
        FeedImpressionServiceImpl service = service();

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogRepository.findAllById(List.of(blogId))).thenReturn(List.of());

        assertThatThrownBy(() -> service.recordImpressions(userId, request(blogId, 1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog not found"));
        verify(feedImpressionRepository, never()).saveAll(any());
    }

    private FeedImpressionServiceImpl service() {
        return new FeedImpressionServiceImpl(feedImpressionRepository, blogRepository, userValidator);
    }

    private FeedImpressionBatchRequestDTO request(UUID blogId, int position) {
        FeedImpressionItemRequestDTO item = new FeedImpressionItemRequestDTO();
        item.setBlogId(blogId);
        item.setPosition(position);
        FeedImpressionBatchRequestDTO request = new FeedImpressionBatchRequestDTO();
        request.setItems(List.of(item));
        return request;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("reader");
        user.setAccountStatus(true);
        return user;
    }

    private Blog blog(UUID blogId) {
        Blog blog = new Blog();
        blog.setId(blogId);
        return blog;
    }
}
