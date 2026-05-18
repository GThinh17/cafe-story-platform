package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ShareType;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.service.serviceImplement.BlogShareServiceImpl;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogShareServiceImplTest {

    @Mock
    private BlogShareRepository blogShareRepository;

    @Mock
    private BlogInteractionMapper blogInteractionMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private BlogShareServiceImpl blogShareService;

    @Test
    void shareBlog_success_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        User user = user(userId);
        BlogShare savedShare = blogShare(UUID.randomUUID(), blog, user, ShareType.PUBLIC);
        BlogShareResponseDTO response = response(savedShare.getId(), blogId, userId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogShareRepository.save(any(BlogShare.class))).thenReturn(savedShare);
        when(blogInteractionMapper.toBlogShareResponseDTO(savedShare)).thenReturn(response);

        BlogShareResponseDTO result = blogShareService.shareBlog(blogId, userId, ShareType.PUBLIC);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getShareCount()).isEqualTo(1);
    }

    @Test
    void shareBlog_fail_pageOnlyWithoutPage_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        User user = user(userId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);

        assertThatThrownBy(() -> blogShareService.shareBlog(blogId, userId, ShareType.PAGE_ONLY))
                .hasMessageContaining("Page only share requires a blog that belongs to a page");

        verify(blogShareRepository, never()).save(any(BlogShare.class));
    }

    @Test
    void getSharesByBlogId_success_TC003() {
        UUID blogId = UUID.randomUUID();
        BlogShare blogShare = blogShare(UUID.randomUUID(), blog(blogId), user(UUID.randomUUID()), ShareType.PUBLIC);
        BlogShareResponseDTO response = response(blogShare.getId(), blogId, blogShare.getUser().getUserId());

        when(blogShareRepository.findByBlogId(blogId)).thenReturn(List.of(blogShare));
        when(blogInteractionMapper.toBlogShareResponseDTO(blogShare)).thenReturn(response);

        List<BlogShareResponseDTO> result = blogShareService.getSharesByBlogId(blogId);

        assertThat(result).containsExactly(response);
        verify(blogValidator).validateBlogExists(blogId);
    }

    @Test
    void getSharesByUserId_success_TC004() {
        UUID userId = UUID.randomUUID();
        BlogShare blogShare = blogShare(UUID.randomUUID(), blog(UUID.randomUUID()), user(userId), ShareType.PUBLIC);
        BlogShareResponseDTO response = response(blogShare.getId(), blogShare.getBlog().getId(), userId);

        when(blogShareRepository.findByUserUserId(userId)).thenReturn(List.of(blogShare));
        when(blogInteractionMapper.toBlogShareResponseDTO(blogShare)).thenReturn(response);

        List<BlogShareResponseDTO> result = blogShareService.getSharesByUserId(userId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(userId);
    }

    private Blog blog(UUID blogId) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setContent("Blog content");
        blog.setShareCount(0);
        return blog;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserPassword("123456");
        user.setUserEmail("luan@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private BlogShare blogShare(UUID id, Blog blog, User user, ShareType shareType) {
        BlogShare blogShare = new BlogShare();
        blogShare.setId(id);
        blogShare.setBlog(blog);
        blogShare.setUser(user);
        blogShare.setShareType(shareType);
        return blogShare;
    }

    private BlogShareResponseDTO response(UUID id, UUID blogId, UUID userId) {
        BlogShareResponseDTO response = new BlogShareResponseDTO();
        response.setId(id);
        response.setBlogId(blogId);
        response.setUserId(userId);
        response.setShareType(ShareType.PUBLIC);
        return response;
    }
}
