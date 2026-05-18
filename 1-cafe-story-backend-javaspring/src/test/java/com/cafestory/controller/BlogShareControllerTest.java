package com.cafestory.controller;

import com.cafestory.dto.requestDTO.BlogShareRequestDTO;
import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.enums.ShareType;
import com.cafestory.service.serviceInterface.BlogShareService;
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
class BlogShareControllerTest {

    @Mock
    private BlogShareService blogShareService;

    @InjectMocks
    private BlogShareController blogShareController;

    @Test
    void shareBlog_success_TC001() {
        UUID blogId = UUID.randomUUID();
        BlogShareRequestDTO request = request();
        BlogShareResponseDTO response = response();

        when(blogShareService.shareBlog(blogId, request.getUserId(), request.getShareType())).thenReturn(response);

        BlogShareResponseDTO result = blogShareController.shareBlog(blogId, request);

        assertThat(result).isEqualTo(response);
        verify(blogShareService).shareBlog(blogId, request.getUserId(), request.getShareType());
    }

    @Test
    void getSharesByBlogId_success_TC002() {
        UUID blogId = UUID.randomUUID();
        List<BlogShareResponseDTO> response = List.of(response());

        when(blogShareService.getSharesByBlogId(blogId)).thenReturn(response);

        List<BlogShareResponseDTO> result = blogShareController.getSharesByBlogId(blogId);

        assertThat(result).isEqualTo(response);
        verify(blogShareService).getSharesByBlogId(blogId);
    }

    @Test
    void getSharesByUserId_success_TC003() {
        UUID userId = UUID.randomUUID();
        List<BlogShareResponseDTO> response = List.of(response());

        when(blogShareService.getSharesByUserId(userId)).thenReturn(response);

        List<BlogShareResponseDTO> result = blogShareController.getSharesByUserId(userId);

        assertThat(result).isEqualTo(response);
        verify(blogShareService).getSharesByUserId(userId);
    }

    private BlogShareRequestDTO request() {
        BlogShareRequestDTO request = new BlogShareRequestDTO();
        request.setUserId(UUID.randomUUID());
        request.setShareType(ShareType.PUBLIC);
        return request;
    }

    private BlogShareResponseDTO response() {
        BlogShareResponseDTO response = new BlogShareResponseDTO();
        response.setId(UUID.randomUUID());
        response.setBlogId(UUID.randomUUID());
        response.setUserId(UUID.randomUUID());
        response.setShareType(ShareType.PUBLIC);
        return response;
    }
}
