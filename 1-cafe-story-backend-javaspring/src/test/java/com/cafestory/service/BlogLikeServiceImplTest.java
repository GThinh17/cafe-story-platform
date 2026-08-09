package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceImplement.BlogLikeServiceImpl;
import com.cafestory.validation.ActorContextResolver;
import com.cafestory.validation.BlogValidator;
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
class BlogLikeServiceImplTest {

    @Mock
    private BlogLikeRepository blogLikeRepository;

    @Mock
    private BlogInteractionMapper blogInteractionMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private ActorContextResolver actorContextResolver;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private BlogLikeServiceImpl blogLikeService;

    @Test
    void likeBlog_success_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.getAuthor().setUserLike(5);
        User user = user(userId);
        BlogLike savedLike = blogLike(UUID.randomUUID(), blog, user);
        BlogLikeResponseDTO response = response(savedLike.getId(), blogId, userId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user, ActorContextType.USER, null));
        when(blogLikeRepository.existsByActor(userId, blogId, ActorContextType.USER, null)).thenReturn(false);
        when(blogLikeRepository.save(any(BlogLike.class))).thenReturn(savedLike);
        when(blogInteractionMapper.toBlogLikeResponseDTO(savedLike)).thenReturn(response);

        BlogLikeResponseDTO result = blogLikeService.likeBlog(blogId, userId);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getLikeCount()).isEqualTo(1);
        assertThat(blog.getAuthor().getUserLike()).isEqualTo(6);
    }

    @Test
    void likeBlog_fail_duplicateLike_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog(blogId));
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user(userId), ActorContextType.USER, null));
        when(blogLikeRepository.existsByActor(userId, blogId, ActorContextType.USER, null)).thenReturn(true);

        assertThatThrownBy(() -> blogLikeService.likeBlog(blogId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog already liked by actor"));

        verify(blogLikeRepository, never()).save(any(BlogLike.class));
    }

    @Test
    void unlikeBlog_success_TC003() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.setLikeCount(2);
        blog.getAuthor().setUserLike(4);
        User user = user(userId);
        BlogLike blogLike = blogLike(UUID.randomUUID(), blog, user);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user, ActorContextType.USER, null));
        when(blogLikeRepository.findByActor(userId, blogId, ActorContextType.USER, null)).thenReturn(Optional.of(blogLike));

        blogLikeService.unlikeBlog(blogId, userId);

        assertThat(blog.getLikeCount()).isEqualTo(1);
        assertThat(blog.getAuthor().getUserLike()).isEqualTo(3);
        verify(blogValidator).validateBlogExists(blogId);
        verify(blogLikeRepository).delete(blogLike);
    }

    @Test
    void unlikeBlog_fail_likeNotFound_TC004() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog(blogId));
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user(userId), ActorContextType.USER, null));
        when(blogLikeRepository.findByActor(userId, blogId, ActorContextType.USER, null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogLikeService.unlikeBlog(blogId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog like not found"));
    }

    @Test
    void getLikesByBlogId_success_TC005() {
        UUID blogId = UUID.randomUUID();
        BlogLike blogLike = blogLike(UUID.randomUUID(), blog(blogId), user(UUID.randomUUID()));
        BlogLikeResponseDTO response = response(blogLike.getId(), blogId, blogLike.getUser().getUserId());

        when(blogLikeRepository.findByBlogId(blogId)).thenReturn(List.of(blogLike));
        when(blogInteractionMapper.toBlogLikeResponseDTO(blogLike)).thenReturn(response);

        List<BlogLikeResponseDTO> result = blogLikeService.getLikesByBlogId(blogId);

        assertThat(result).containsExactly(response);
        verify(blogValidator).validateBlogExists(blogId);
    }

    @Test
    void getLikesByUserId_success_TC006() {
        UUID userId = UUID.randomUUID();
        BlogLike blogLike = blogLike(UUID.randomUUID(), blog(UUID.randomUUID()), user(userId));
        BlogLikeResponseDTO response = response(blogLike.getId(), blogLike.getBlog().getId(), userId);

        when(blogLikeRepository.findByUserUserId(userId)).thenReturn(List.of(blogLike));
        when(blogInteractionMapper.toBlogLikeResponseDTO(blogLike)).thenReturn(response);

        List<BlogLikeResponseDTO> result = blogLikeService.getLikesByUserId(userId);

        assertThat(result).containsExactly(response);
        verify(userValidator).validateUserExists(userId);
    }

    @Test
    void likeBlog_success_cafePageActorAndNullCounters_TC007() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.setLikeCount(null);
        blog.getAuthor().setUserLike(null);
        User user = user(userId);
        com.cafestory.entity.CafePage cafePage = new com.cafestory.entity.CafePage();
        cafePage.setId(UUID.randomUUID());
        BlogLike savedLike = blogLike(UUID.randomUUID(), blog, user);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, ActorContextType.CAFE_PAGE, cafePage.getId()))
                .thenReturn(new ActorContext(user, ActorContextType.CAFE_PAGE, cafePage));
        when(blogLikeRepository.existsByActor(userId, blogId, ActorContextType.CAFE_PAGE, cafePage.getId()))
                .thenReturn(false);
        when(blogLikeRepository.save(any(BlogLike.class))).thenReturn(savedLike);
        when(blogInteractionMapper.toBlogLikeResponseDTO(savedLike))
                .thenReturn(response(savedLike.getId(), blogId, userId));

        blogLikeService.likeBlog(blogId, userId, ActorContextType.CAFE_PAGE, cafePage.getId());

        assertThat(blog.getLikeCount()).isEqualTo(1);
        assertThat(blog.getAuthor().getUserLike()).isEqualTo(1);
    }

    @Test
    void unlikeBlog_success_countersNeverGoNegativeAndAuthorMayBeMissing_TC008() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.setLikeCount(null);
        blog.setAuthor(null);
        User user = user(userId);
        BlogLike existingLike = blogLike(UUID.randomUUID(), blog, user);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user, ActorContextType.USER, null));
        when(blogLikeRepository.findByActor(userId, blogId, ActorContextType.USER, null))
                .thenReturn(java.util.Optional.of(existingLike));

        blogLikeService.unlikeBlog(blogId, userId);

        assertThat(blog.getLikeCount()).isZero();
        verify(blogLikeRepository).delete(existingLike);
    }

    @Test
    void likeBlog_success_authorMissingSkipsUserLikeCounter_TC009() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.setAuthor(null);
        User user = user(userId);
        BlogLike savedLike = blogLike(UUID.randomUUID(), blog, user);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(actorContextResolver.resolve(userId, ActorContextType.USER, null))
                .thenReturn(new ActorContext(user, ActorContextType.USER, null));
        when(blogLikeRepository.existsByActor(userId, blogId, ActorContextType.USER, null)).thenReturn(false);
        when(blogLikeRepository.save(any(BlogLike.class))).thenReturn(savedLike);
        when(blogInteractionMapper.toBlogLikeResponseDTO(savedLike))
                .thenReturn(response(savedLike.getId(), blogId, userId));

        blogLikeService.likeBlog(blogId, userId);

        assertThat(blog.getLikeCount()).isEqualTo(1);
    }

    private Blog blog(UUID blogId) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(user(UUID.randomUUID()));
        blog.setContent("Blog content");
        blog.setLikeCount(0);
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

    private BlogLike blogLike(UUID id, Blog blog, User user) {
        BlogLike blogLike = new BlogLike();
        blogLike.setId(id);
        blogLike.setBlog(blog);
        blogLike.setUser(user);
        return blogLike;
    }

    private BlogLikeResponseDTO response(UUID id, UUID blogId, UUID userId) {
        BlogLikeResponseDTO response = new BlogLikeResponseDTO();
        response.setId(id);
        response.setBlogId(blogId);
        response.setUserId(userId);
        return response;
    }
}
