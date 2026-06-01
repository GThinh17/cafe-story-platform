package com.cafestory.service;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRating;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.service.serviceImplement.BlogServiceImpl;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private BlogSaveRepository blogSaveRepository;

    @Mock
    private BlogRatingRepository blogRatingRepository;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private BlogServiceImpl blogService;

    @Test
    void createBlog_success_TC001() {
        BlogCreateDTO request = createBlogRequest();
        UUID actorUserId = UUID.randomUUID();
        request.setAuthorUserId(UUID.randomUUID());
        User author = user(actorUserId);
        CafePage page = cafePage(request.getPageId());
        Blog blog = blog();
        Blog savedBlog = blog();
        BlogResponseDTO response = blogResponse(savedBlog.getId(), actorUserId);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        when(cafePageValidator.validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId))
                .thenReturn(page);
        when(blogMapper.toBlog(request)).thenReturn(blog);
        when(blogRepository.save(blog)).thenReturn(savedBlog);
        when(blogMapper.toBlogResponseDTO(savedBlog)).thenReturn(response);

        BlogResponseDTO result = blogService.createBlog(request, actorUserId);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getAuthor()).isEqualTo(author);
        assertThat(blog.getPage()).isEqualTo(page);
        assertThat(blog.getPageId()).isEqualTo(request.getPageId());
        assertThat(blog.getIsPinned()).isTrue();
        assertThat(blog.getAllowComment()).isFalse();
        verify(userValidator).validateUserActive(author);
        verify(cafePageValidator).validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId);
        verify(blogRepository).save(blog);
    }

    @Test
    void createBlog_success_defaultBooleanFields_TC002() {
        BlogCreateDTO request = createBlogRequest();
        request.setPageId(null);
        request.setIsPinned(null);
        request.setAllowComment(null);
        UUID actorUserId = UUID.randomUUID();
        User author = user(actorUserId);
        Blog blog = blog();
        blog.setIsPinned(false);
        blog.setAllowComment(true);
        BlogResponseDTO response = blogResponse(blog.getId(), actorUserId);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        when(blogMapper.toBlog(request)).thenReturn(blog);
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogResponseDTO result = blogService.createBlog(request, actorUserId);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getIsPinned()).isFalse();
        assertThat(blog.getAllowComment()).isTrue();
        verify(userValidator).validateUserActive(author);
        verify(cafePageValidator, never()).validateUserCanCreateBlogOnPage(any(), any());
    }

    @Test
    void createBlog_fail_authorNotAllowedOnPage_TC003() {
        BlogCreateDTO request = createBlogRequest();
        UUID actorUserId = UUID.randomUUID();
        User author = user(actorUserId);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        org.mockito.Mockito.doThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User is not allowed to create blog on this cafe page"))
                .when(cafePageValidator)
                .validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId);

        assertThatThrownBy(() -> blogService.createBlog(request, actorUserId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void createBlog_fail_authorNotFound_TC004() {
        BlogCreateDTO request = createBlogRequest();
        UUID actorUserId = UUID.randomUUID();

        when(userValidator.validateUserExists(actorUserId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() -> blogService.createBlog(request, actorUserId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void getAllBlogs_success_TC005() {
        Blog blog = blog();
        BlogResponseDTO response = blogResponse(blog.getId(), UUID.randomUUID());

        when(blogRepository.findAll()).thenReturn(List.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        List<BlogResponseDTO> result = blogService.getAllBlogs();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getBlogsByAuthorId_success_TC006() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();
        BlogResponseDTO response = blogResponse(blog.getId(), userId);

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        List<BlogResponseDTO> result = blogService.getBlogsByAuthorId(userId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(userId);
    }

    @Test
    void getAllBlogsByUserId_success_TC007() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();
        BlogResponseDTO response = blogResponse(blog.getId(), userId);

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId);

        assertThat(result).containsExactly(response);
        verify(blogRepository).findByAuthorUserId(userId);
    }

    @Test
    void getAllBlogsByUserId_fail_nullUserId_TC008() {
        when(userValidator.validateUserExists(null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required"));

        assertThatThrownBy(() -> blogService.getAllBlogsByUserId(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User id is required"));

        verify(blogRepository, never()).findByAuthorUserId(null);
    }

    @Test
    void getAllBlogsByUserId_fail_userNotFound_TC009() {
        UUID userId = UUID.randomUUID();

        when(userValidator.validateUserExists(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() -> blogService.getAllBlogsByUserId(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("User not found"));

        verify(blogRepository, never()).findByAuthorUserId(userId);
    }

    @Test
    void getBlogById_success_TC010() {
        UUID blogId = UUID.randomUUID();
        Blog blog = blog();
        BlogResponseDTO response = blogResponse(blogId, UUID.randomUUID());

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogResponseDTO result = blogService.getBlogById(blogId);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getBlogById_success_enrichesViewerInteractionFields_TC011() {
        UUID blogId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        Blog blog = blog();
        blog.setId(blogId);
        BlogRating rating = rating(blog, user(viewerUserId), 4);
        BlogResponseDTO response = blogResponse(blogId, blog.getAuthor().getUserId());

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);
        when(blogLikeRepository.existsByUserUserIdAndBlogId(viewerUserId, blogId)).thenReturn(true);
        when(blogSaveRepository.findByUserUserIdAndBlogId(viewerUserId, blogId))
                .thenReturn(Optional.of(new com.cafestory.entity.BlogSave()));
        when(blogSaveRepository.countByBlogId(blogId)).thenReturn(3L);
        when(blogRatingRepository.findAverageRatingByBlogId(blogId)).thenReturn(4.5);
        when(blogRatingRepository.countByBlogId(blogId)).thenReturn(2L);
        when(blogRatingRepository.findByUserUserIdAndBlogId(viewerUserId, blogId)).thenReturn(Optional.of(rating));

        BlogResponseDTO result = blogService.getBlogById(blogId, viewerUserId);

        assertThat(result.getIsLike()).isTrue();
        assertThat(result.getIsSave()).isTrue();
        assertThat(result.getIsRating()).isTrue();
        assertThat(result.getMyRating()).isEqualTo(4);
        assertThat(result.getRatingScore()).isEqualTo(4.5);
        assertThat(result.getRatingCount()).isEqualTo(2L);
        assertThat(result.getSaveCount()).isEqualTo(3L);
    }

    @Test
    void getBlogById_fail_blogNotFound_TC012() {
        UUID blogId = UUID.randomUUID();

        when(blogValidator.validateBlogExists(blogId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        assertThatThrownBy(() -> blogService.getBlogById(blogId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateBlog_success_updateAllFields_TC013() {
        UUID blogId = UUID.randomUUID();
        BlogUpdateDTO request = updateBlogRequest();
        Blog blog = blog();
        UUID actorUserId = blog.getAuthor().getUserId();
        BlogResponseDTO response = blogResponse(blogId, UUID.randomUUID());

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(cafePageValidator.validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId))
                .thenReturn(cafePage(request.getPageId()));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogResponseDTO result = blogService.updateBlog(blogId, actorUserId, request);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getPageId()).isEqualTo(request.getPageId());
        assertThat(blog.getPage()).isNotNull();
        assertThat(blog.getRegionId()).isEqualTo(request.getRegionId());
        assertThat(blog.getContent()).isEqualTo("Updated blog content");
        assertThat(blog.getImageUrls()).containsExactly("https://example.com/updated-1.png");
        assertThat(blog.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(blog.getIsPinned()).isTrue();
        assertThat(blog.getAllowComment()).isFalse();
        verify(blogRepository).save(blog);
    }

    @Test
    void updateBlog_success_nullFields_TC014() {
        UUID blogId = UUID.randomUUID();
        BlogUpdateDTO request = new BlogUpdateDTO();
        Blog blog = blog();
        UUID actorUserId = blog.getAuthor().getUserId();
        BlogResponseDTO response = blogResponse(blogId, UUID.randomUUID());

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogResponseDTO result = blogService.updateBlog(blogId, actorUserId, request);

        assertThat(result).isEqualTo(response);
        verify(blogRepository).save(blog);
    }

    @Test
    void updateBlog_fail_blogNotFound_TC015() {
        UUID blogId = UUID.randomUUID();
        BlogUpdateDTO request = updateBlogRequest();

        when(blogValidator.validateBlogExists(blogId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        assertThatThrownBy(() -> blogService.updateBlog(blogId, UUID.randomUUID(), request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void deleteBlog_success_TC016() {
        UUID blogId = UUID.randomUUID();
        Blog blog = blog();
        UUID actorUserId = blog.getAuthor().getUserId();

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);

        blogService.deleteBlog(blogId, actorUserId);

        verify(blogRepository).delete(blog);
    }

    @Test
    void deleteBlog_fail_blogNotFound_TC017() {
        UUID blogId = UUID.randomUUID();

        when(blogValidator.validateBlogExists(blogId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        assertThatThrownBy(() -> blogService.deleteBlog(blogId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(blogRepository, never()).delete(any(Blog.class));
    }

    private BlogCreateDTO createBlogRequest() {
        BlogCreateDTO request = new BlogCreateDTO();
        request.setAuthorUserId(UUID.randomUUID());
        request.setPageId(UUID.randomUUID());
        request.setRegionId(UUID.randomUUID());
        request.setContent("Cafe review content");
        request.setImageUrls(List.of("https://example.com/blog-1.png"));
        request.setIsPinned(true);
        request.setAllowComment(false);
        return request;
    }

    private BlogUpdateDTO updateBlogRequest() {
        BlogUpdateDTO request = new BlogUpdateDTO();
        request.setPageId(UUID.randomUUID());
        request.setRegionId(UUID.randomUUID());
        request.setContent("Updated blog content");
        request.setImageUrls(List.of("https://example.com/updated-1.png"));
        request.setStatus(PostStatus.HIDDEN);
        request.setIsPinned(true);
        request.setAllowComment(false);
        return request;
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(user(UUID.randomUUID()));
        blog.setPageId(UUID.randomUUID());
        blog.setRegionId(UUID.randomUUID());
        blog.setContent("Cafe review content");
        blog.setImageUrls(List.of("https://example.com/blog-1.png"));
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setIsPinned(false);
        blog.setAllowComment(true);
        return blog;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("luan123");
        user.setUserFullName("Nguyen Van Luan");
        user.setUserPassword("123456");
        user.setUserEmail("luan123@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private BlogRating rating(Blog blog, User user, Integer ratingValue) {
        BlogRating rating = new BlogRating();
        rating.setId(UUID.randomUUID());
        rating.setBlog(blog);
        rating.setUser(user);
        rating.setRating(ratingValue);
        return rating;
    }

    private CafePage cafePage(UUID pageId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(pageId);
        cafePage.setName("Cafe Story Roastery");
        cafePage.setAvatarUrl("https://example.com/cafe-avatar.png");
        return cafePage;
    }

    private BlogResponseDTO blogResponse(UUID blogId, UUID authorUserId) {
        BlogResponseDTO response = new BlogResponseDTO();
        response.setId(blogId);
        response.setAuthorUserId(authorUserId);
        response.setPageId(UUID.randomUUID());
        response.setRegionId(UUID.randomUUID());
        response.setContent("Cafe review content");
        response.setImageUrls(List.of("https://example.com/blog-1.png"));
        response.setStatus(PostStatus.PUBLISHED);
        response.setIsPinned(false);
        response.setAllowComment(true);
        return response;
    }
}
