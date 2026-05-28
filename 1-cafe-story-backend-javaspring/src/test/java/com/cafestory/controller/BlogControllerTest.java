package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
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
class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @InjectMocks
    private BlogController blogController;

    @Test
    void createBlog_success_TC001() {
        UUID userId = UUID.randomUUID();
        BlogCreateDTO request = createBlogRequest();
        BlogResponseDTO response = blogResponse();

        when(blogService.createBlog(request, userId)).thenReturn(response);

        BlogResponseDTO result = blogController.createBlog(request, principal(userId));

        assertThat(result).isEqualTo(response);
        assertThat(request.getAuthorUserId()).isNull();
        verify(blogService).createBlog(request, userId);
    }

    @Test
    void getBlogs_success_getAllBlogs_TC002() {
        List<BlogResponseDTO> response = List.of(blogResponse());

        when(blogService.getAllBlogs()).thenReturn(response);

        List<BlogResponseDTO> result = blogController.getBlogs(null);

        assertThat(result).isEqualTo(response);
        verify(blogService).getAllBlogs();
    }

    @Test
    void getBlogs_success_getAllBlogsByAuthorUserId_TC003() {
        UUID authorUserId = UUID.randomUUID();
        List<BlogResponseDTO> response = List.of(blogResponse());

        when(blogService.getAllBlogsByUserId(authorUserId)).thenReturn(response);

        List<BlogResponseDTO> result = blogController.getBlogs(authorUserId);

        assertThat(result).isEqualTo(response);
        verify(blogService).getAllBlogsByUserId(authorUserId);
    }

    @Test
    void getAllBlogsByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        List<BlogResponseDTO> response = List.of(blogResponse());

        when(blogService.getAllBlogsByUserId(userId)).thenReturn(response);

        List<BlogResponseDTO> result = blogController.getAllBlogsByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(blogService).getAllBlogsByUserId(userId);
    }

    @Test
    void getBlogById_success_TC005() {
        UUID blogId = UUID.randomUUID();
        BlogResponseDTO response = blogResponse();

        when(blogService.getBlogById(blogId)).thenReturn(response);

        BlogResponseDTO result = blogController.getBlogById(blogId);

        assertThat(result).isEqualTo(response);
        verify(blogService).getBlogById(blogId);
    }

    @Test
    void updateBlog_success_TC006() {
        UUID userId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        BlogUpdateDTO request = updateBlogRequest();
        BlogResponseDTO response = blogResponse();

        when(blogService.updateBlog(blogId, userId, request)).thenReturn(response);

        BlogResponseDTO result = blogController.updateBlog(blogId, request, principal(userId));

        assertThat(result).isEqualTo(response);
        verify(blogService).updateBlog(blogId, userId, request);
    }

    @Test
    void deleteBlog_success_TC007() {
        UUID userId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();

        blogController.deleteBlog(blogId, principal(userId));

        verify(blogService).deleteBlog(blogId, userId);
    }

    private BlogCreateDTO createBlogRequest() {
        BlogCreateDTO request = new BlogCreateDTO();
        request.setPageId(UUID.randomUUID());
        request.setRegionId(UUID.randomUUID());
        request.setContent("Cafe review content");
        request.setImageUrls(List.of("https://example.com/blog-1.png"));
        request.setIsPinned(false);
        request.setAllowComment(true);
        return request;
    }

    private BlogUpdateDTO updateBlogRequest() {
        BlogUpdateDTO request = new BlogUpdateDTO();
        request.setContent("Updated blog content");
        request.setStatus(PostStatus.HIDDEN);
        request.setImageUrls(List.of("https://example.com/updated-1.png"));
        return request;
    }

    private BlogResponseDTO blogResponse() {
        BlogResponseDTO response = new BlogResponseDTO();
        response.setId(UUID.randomUUID());
        response.setAuthorUserId(UUID.randomUUID());
        response.setPageId(UUID.randomUUID());
        response.setRegionId(UUID.randomUUID());
        response.setContent("Cafe review content");
        response.setImageUrls(List.of("https://example.com/blog-1.png"));
        response.setStatus(PostStatus.PUBLISHED);
        response.setIsPinned(false);
        response.setAllowComment(true);
        return response;
    }

    private AuthenticatedUserPrincipal principal(UUID userId) {
        return new AuthenticatedUserPrincipal(userId, "tester", List.of("USER"));
    }
}
