package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogInteractionRequestDTO;
import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.service.serviceInterface.BlogLikeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogLikeControllerTest {

    @Mock
    private BlogLikeService blogLikeService;

    @InjectMocks
    private BlogLikeController blogLikeController;

    @Test
    void likeBlog_success_TC001() {
        UUID blogId = UUID.randomUUID();
        BlogInteractionRequestDTO request = request();
        BlogLikeResponseDTO response = response();

        when(blogLikeService.likeBlog(blogId, request.getUserId())).thenReturn(response);

        BlogLikeResponseDTO result = blogLikeController.likeBlog(blogId, request);

        assertThat(result).isEqualTo(response);
        verify(blogLikeService).likeBlog(blogId, request.getUserId());
    }

    @Test
    void unlikeBlog_success_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        blogLikeController.unlikeBlog(blogId, userId);

        verify(blogLikeService).unlikeBlog(blogId, userId);
    }

    @Test
    void getLikesByBlogId_success_TC003() {
        UUID blogId = UUID.randomUUID();
        List<BlogLikeResponseDTO> response = List.of(response());

        when(blogLikeService.getLikesByBlogId(blogId)).thenReturn(response);

        List<BlogLikeResponseDTO> result = blogLikeController.getLikesByBlogId(blogId);

        assertThat(result).isEqualTo(response);
        verify(blogLikeService).getLikesByBlogId(blogId);
    }

    @Test
    void getLikesByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        List<BlogLikeResponseDTO> response = List.of(response());

        when(blogLikeService.getLikesByUserId(userId)).thenReturn(response);

        List<BlogLikeResponseDTO> result = blogLikeController.getLikesByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(blogLikeService).getLikesByUserId(userId);
    }

    private BlogInteractionRequestDTO request() {
        BlogInteractionRequestDTO request = new BlogInteractionRequestDTO();
        request.setUserId(UUID.randomUUID());
        return request;
    }

    private BlogLikeResponseDTO response() {
        BlogLikeResponseDTO response = new BlogLikeResponseDTO();
        response.setId(UUID.randomUUID());
        response.setBlogId(UUID.randomUUID());
        response.setUserId(UUID.randomUUID());
        return response;
    }
}
