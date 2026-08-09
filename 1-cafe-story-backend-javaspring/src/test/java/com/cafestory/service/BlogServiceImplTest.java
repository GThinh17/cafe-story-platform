package com.cafestory.service;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRating;
import com.cafestory.entity.BlogTaggedUser;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogTaggedUserRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceImplement.BlogServiceImpl;
import com.cafestory.service.serviceInterface.AiBlogModerationService;
import com.cafestory.service.serviceInterface.BlogModerationTagService;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private BlogTaggedUserRepository blogTaggedUserRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private BlogTagService blogTagService;

    @Mock
    private BlogModerationTagService blogModerationTagService;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private AiBlogModerationService aiBlogModerationService;

    @Mock
    private RegionService regionService;

    private BlogServiceImpl blogService;

    @BeforeEach
    void setUp() {
        blogService = new BlogServiceImpl(
                blogRepository,
                blogLikeRepository,
                blogSaveRepository,
                blogRatingRepository,
                blogTaggedUserRepository,
                regionRepository,
                regionService,
                aiBlogModerationService,
                blogMapper,
                blogValidator,
                cafePageValidator,
                userValidator,
                blogTagService,
                blogModerationTagService,
                userFollowRepository,
                pageFollowRepository);
    }

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
        Region region = region(request.getRegionId());

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        when(cafePageValidator.validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId))
                .thenReturn(page);
        when(blogMapper.toBlog(request)).thenReturn(blog);
        when(regionService.resolveExistingRegion(request.getRegionId(), RegionRequirement.BLOG_LOCATION))
                .thenReturn(region);
        when(blogRepository.save(blog)).thenReturn(savedBlog);
        when(blogMapper.toBlogResponseDTO(savedBlog)).thenReturn(response);

        BlogResponseDTO result = blogService.createBlog(request, actorUserId);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getAuthor()).isEqualTo(author);
        assertThat(blog.getPage()).isEqualTo(page);
        assertThat(blog.getPage().getId()).isEqualTo(request.getPageId());
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
        Region region = region(request.getRegionId());

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        when(blogMapper.toBlog(request)).thenReturn(blog);
        when(regionService.resolveExistingRegion(request.getRegionId(), RegionRequirement.BLOG_LOCATION))
                .thenReturn(region);
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
    void createModeratedBlog_success_callsAiModeration_TC004A() {
        BlogCreateDTO request = createBlogRequest();
        UUID actorUserId = UUID.randomUUID();
        User author = user(actorUserId);
        Blog blog = blog();
        Blog savedBlog = blog();
        savedBlog.setStatus(PostStatus.PUBLISHED);
        Blog moderatedBlog = blog();
        moderatedBlog.setStatus(PostStatus.HIDDEN);
        BlogResponseDTO response = blogResponse(moderatedBlog.getId(), actorUserId);
        response.setStatus(PostStatus.HIDDEN);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(author);
        when(cafePageValidator.validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId))
                .thenReturn(cafePage(request.getPageId()));
        when(blogMapper.toBlog(request)).thenReturn(blog);
        when(regionService.resolveExistingRegion(request.getRegionId(), RegionRequirement.BLOG_LOCATION))
                .thenReturn(region(request.getRegionId()));
        when(blogRepository.save(blog)).thenReturn(savedBlog);
        when(aiBlogModerationService.moderateBlog(savedBlog)).thenReturn(moderatedBlog);
        when(blogMapper.toBlogResponseDTO(moderatedBlog)).thenReturn(response);

        BlogResponseDTO result = blogService.createModeratedBlog(request, actorUserId);

        assertThat(result).isEqualTo(response);
        assertThat(result.getStatus()).isEqualTo(PostStatus.HIDDEN);
        verify(blogRepository).save(blog);
        verify(blogTagService).syncBlogTags(savedBlog, actorUserId, request.getTaggedUserIds());
        verify(aiBlogModerationService).moderateBlog(savedBlog);
    }

    @Test
    void getAllBlogs_success_TC005() {
        Blog blog = blog();

        when(blogRepository.findAll()).thenReturn(List.of(blog));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId())))
                .thenReturn(List.of(imageRow(blog.getId(), "https://example.com/blog-1.png")));
        when(regionRepository.findAllById(List.of(blog.getRegionId()))).thenReturn(List.of(region(blog.getRegionId())));

        List<BlogResponseDTO> result = blogService.getAllBlogs();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(blog.getId());
        assertThat(result.getFirst().getImageUrls()).containsExactly("https://example.com/blog-1.png");
        assertThat(result.getFirst().getRegionCity()).isEqualTo("Ho Chi Minh");
        verify(blogMapper, never()).toBlogResponseDTO(blog);
    }

    @Test
    void getBlogsByAuthorId_success_TC006() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();
        blog.setAuthor(user(userId));

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));

        List<BlogResponseDTO> result = blogService.getBlogsByAuthorId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getAuthorUserId()).isEqualTo(userId);
        verify(userValidator).validateUserExists(userId);
    }

    @Test
    void getAllBlogsByUserId_success_TC007() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();
        UUID viewerUserId = UUID.randomUUID();
        BlogTaggedUser tag = blogTag(blog, user(UUID.randomUUID()));

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId())))
                .thenReturn(List.of(imageRow(blog.getId(), "https://example.com/blog-1.png")));
        when(regionRepository.findAllById(List.of(blog.getRegionId()))).thenReturn(List.of(region(blog.getRegionId())));
        when(blogLikeRepository.findLikedBlogIdsByUserIdAndBlogIds(viewerUserId, List.of(blog.getId())))
                .thenReturn(List.of(blog.getId()));
        when(blogSaveRepository.findSavedBlogIdsByUserIdAndBlogIds(viewerUserId, List.of(blog.getId())))
                .thenReturn(List.of(blog.getId()));
        when(blogSaveRepository.countSavesByBlogIds(List.of(blog.getId())))
                .thenReturn(List.of(countRow(blog.getId(), 3L)));
        when(blogRatingRepository.findRatingSummariesByBlogIds(List.of(blog.getId())))
                .thenReturn(List.of(ratingSummaryRow(blog.getId(), 4.5, 2L)));
        when(blogRatingRepository.findUserRatingsByUserIdAndBlogIds(viewerUserId, List.of(blog.getId())))
                .thenReturn(List.of(userRatingRow(blog.getId(), 4)));
        when(blogTaggedUserRepository.findByBlogIdInWithTaggedUser(List.of(blog.getId())))
                .thenReturn(List.of(tag));

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId, viewerUserId);

        assertThat(result).hasSize(1);
        BlogResponseDTO response = result.getFirst();
        assertThat(response.getId()).isEqualTo(blog.getId());
        assertThat(response.getIsLike()).isTrue();
        assertThat(response.getIsSave()).isTrue();
        assertThat(response.getSaveCount()).isEqualTo(3L);
        assertThat(response.getRatingScore()).isEqualTo(4.5);
        assertThat(response.getRatingCount()).isEqualTo(2L);
        assertThat(response.getIsRating()).isTrue();
        assertThat(response.getMyRating()).isEqualTo(4);
        assertThat(response.getTaggedUsers()).hasSize(1);
        assertThat(response.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        verify(blogRepository).findByAuthorUserId(userId);
        verify(blogMapper, never()).toBlogResponseDTO(blog);
        verify(blogLikeRepository, never()).existsByUserUserIdAndBlogId(viewerUserId, blog.getId());
        verify(blogSaveRepository, never()).findByUserUserIdAndBlogId(viewerUserId, blog.getId());
        verify(blogSaveRepository, never()).countByBlogId(blog.getId());
        verify(blogRatingRepository, never()).findAverageRatingByBlogId(blog.getId());
        verify(blogRatingRepository, never()).countByBlogId(blog.getId());
        verify(blogRatingRepository, never()).findByUserUserIdAndBlogId(viewerUserId, blog.getId());
        verify(blogTagService, never()).getTaggedUsers(blog.getId());
    }

    @Test
    void getAllBlogsByUserId_success_anonSkipsViewerStateQueries_TC007A() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getIsLike()).isFalse();
        assertThat(result.getFirst().getIsSave()).isFalse();
        assertThat(result.getFirst().getIsRating()).isFalse();
        assertThat(result.getFirst().getMyRating()).isNull();
        verify(blogLikeRepository, never()).findLikedBlogIdsByUserIdAndBlogIds(any(), any());
        verify(blogSaveRepository, never()).findSavedBlogIdsByUserIdAndBlogIds(any(), any());
        verify(blogRatingRepository, never()).findUserRatingsByUserIdAndBlogIds(any(), any());
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
    void getAllBlogsByUserIdWithStatus_success_ownerRequestsHidden_TC009A() {
        UUID userId = UUID.randomUUID();
        Blog hidden = blog();
        hidden.setStatus(PostStatus.HIDDEN);

        when(blogRepository.findByAuthorUserIdAndStatus(userId, PostStatus.HIDDEN))
                .thenReturn(List.of(hidden));

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId, userId, PostStatus.HIDDEN);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(hidden.getId());
        verify(blogRepository).findByAuthorUserIdAndStatus(userId, PostStatus.HIDDEN);
        verify(blogRepository, never()).findByAuthorUserId(userId);
    }

    @Test
    void getAllBlogsByUserIdWithStatus_fail_nonOwnerRequestsHidden_TC009B() {
        UUID userId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();

        assertThatThrownBy(() ->
                blogService.getAllBlogsByUserId(userId, viewerUserId, PostStatus.HIDDEN))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(blogRepository, never()).findByAuthorUserIdAndStatus(any(), any());
        verify(blogRepository, never()).findByAuthorUserId(any());
    }

    @Test
    void getAllBlogsByUserIdWithStatus_success_publishedAllowedForNonOwner_TC009C() {
        UUID userId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        Blog published = blog();

        when(blogRepository.findByAuthorUserIdAndStatus(userId, PostStatus.PUBLISHED))
                .thenReturn(List.of(published));

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId, viewerUserId, PostStatus.PUBLISHED);

        assertThat(result).hasSize(1);
        verify(blogRepository).findByAuthorUserIdAndStatus(userId, PostStatus.PUBLISHED);
    }

    @Test
    void getAllBlogsByUserIdWithStatus_success_nullStatusStripsNonPublishedForNonOwner_TC009D() {
        UUID userId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        Blog published = blog();
        Blog hidden = blog();
        hidden.setStatus(PostStatus.HIDDEN);

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(published, hidden));

        List<BlogResponseDTO> result = blogService.getAllBlogsByUserId(userId, viewerUserId, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(published.getId());
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
        Region region = region(request.getRegionId());

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(cafePageValidator.validateUserCanCreateBlogOnPage(request.getPageId(), actorUserId))
                .thenReturn(cafePage(request.getPageId()));
        when(regionService.resolveExistingRegion(request.getRegionId(), RegionRequirement.BLOG_LOCATION))
                .thenReturn(region);
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(response);

        BlogResponseDTO result = blogService.updateBlog(blogId, actorUserId, request);

        assertThat(result).isEqualTo(response);
        assertThat(blog.getPage().getId()).isEqualTo(request.getPageId());
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

    @Test
    void getSharedBlogsByUserId_success_shareCountSortUsesDedicatedQuery_TC018() {
        UUID userId = UUID.randomUUID();
        Blog blog = blog();

        when(blogRepository.findSharedBlogsByUserIdOrderByShareCount(userId)).thenReturn(List.of(blog, blog));
        when(blogRepository.findImageUrlsByBlogIds(List.of(blog.getId()))).thenReturn(List.of());
        when(regionRepository.findAllById(List.of(blog.getRegionId()))).thenReturn(List.of());
        when(blogSaveRepository.countSavesByBlogIds(List.of(blog.getId()))).thenReturn(List.of());
        when(blogRatingRepository.findRatingSummariesByBlogIds(List.of(blog.getId()))).thenReturn(List.of());
        when(blogTaggedUserRepository.findByBlogIdInWithTaggedUser(List.of(blog.getId()))).thenReturn(List.of());

        // Bài trùng id bị gộp lại còn một bản ghi.
        assertThat(blogService.getSharedBlogsByUserId(userId, null, "shareCount")).hasSize(1);
        verify(blogRepository, never()).findSharedBlogsByUserId(userId);
    }

    @Test
    void getSharedBlogsByUserId_success_defaultSortUsesRecentQuery_TC019() {
        UUID userId = UUID.randomUUID();

        when(blogRepository.findSharedBlogsByUserId(userId)).thenReturn(List.of());

        assertThat(blogService.getSharedBlogsByUserId(userId, null, null)).isEmpty();
        assertThat(blogService.getSharedBlogsByUserId(userId, null, "recent")).isEmpty();
        verify(blogRepository, never()).findSharedBlogsByUserIdOrderByShareCount(userId);
    }

    @Test
    void getAllBlogsByUserId_success_blogWithoutIdIsSkipped_TC020() {
        UUID userId = UUID.randomUUID();
        Blog withoutId = blog();
        withoutId.setId(null);

        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(withoutId));

        assertThat(blogService.getAllBlogsByUserId(userId, null)).isEmpty();
        verify(blogRepository, never()).findImageUrlsByBlogIds(any());
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

    private BlogTaggedUser blogTag(Blog blog, User taggedUser) {
        BlogTaggedUser tag = new BlogTaggedUser();
        tag.setId(UUID.randomUUID());
        tag.setBlog(blog);
        tag.setTaggedUser(taggedUser);
        return tag;
    }

    private BlogRepository.BlogImageUrlRow imageRow(UUID blogId, String imageUrl) {
        return new BlogRepository.BlogImageUrlRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public String getImageUrl() {
                return imageUrl;
            }
        };
    }

    private BlogSaveRepository.BlogCountRow countRow(UUID blogId, Long count) {
        return new BlogSaveRepository.BlogCountRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public Long getCount() {
                return count;
            }
        };
    }

    private BlogRatingRepository.BlogRatingSummaryRow ratingSummaryRow(UUID blogId, Double averageRating, Long ratingCount) {
        return new BlogRatingRepository.BlogRatingSummaryRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public Double getAverageRating() {
                return averageRating;
            }

            @Override
            public Long getRatingCount() {
                return ratingCount;
            }
        };
    }

    private BlogRatingRepository.BlogUserRatingRow userRatingRow(UUID blogId, Integer rating) {
        return new BlogRatingRepository.BlogUserRatingRow() {
            @Override
            public UUID getBlogId() {
                return blogId;
            }

            @Override
            public Integer getRating() {
                return rating;
            }
        };
    }

    private CafePage cafePage(UUID pageId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(pageId);
        cafePage.setName("Cafe Story Roastery");
        cafePage.setAvatarUrl("https://example.com/cafe-avatar.png");
        return cafePage;
    }

    private Region region(UUID regionId) {
        Region region = new Region();
        region.setRegionId(regionId);
        region.setCity("Ho Chi Minh");
        region.setProvince("Ho Chi Minh");
        return region;
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
