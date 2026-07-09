package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogTaggedUser;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogTaggedUserRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceInterface.AiBlogModerationService;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogSaveRepository blogSaveRepository;
    private final BlogRatingRepository blogRatingRepository;
    private final BlogTaggedUserRepository blogTaggedUserRepository;
    private final RegionRepository regionRepository;
    private final RegionService regionService;
    private final AiBlogModerationService aiBlogModerationService;
    private final BlogMapper blogMapper;
    private final BlogValidator blogValidator;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;
    private final BlogTagService blogTagService;
    private final UserFollowRepository userFollowRepository;
    private final PageFollowRepository pageFollowRepository;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            BlogLikeRepository blogLikeRepository,
            BlogSaveRepository blogSaveRepository,
            BlogRatingRepository blogRatingRepository,
            BlogTaggedUserRepository blogTaggedUserRepository,
            RegionRepository regionRepository,
            RegionService regionService,
            AiBlogModerationService aiBlogModerationService,
            BlogMapper blogMapper,
            BlogValidator blogValidator,
            CafePageValidator cafePageValidator,
            UserValidator userValidator,
            BlogTagService blogTagService,
            UserFollowRepository userFollowRepository,
            PageFollowRepository pageFollowRepository) {
        this.blogRepository = blogRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.blogSaveRepository = blogSaveRepository;
        this.blogRatingRepository = blogRatingRepository;
        this.blogTaggedUserRepository = blogTaggedUserRepository;
        this.regionRepository = regionRepository;
        this.regionService = regionService;
        this.aiBlogModerationService = aiBlogModerationService;
        this.blogMapper = blogMapper;
        this.blogValidator = blogValidator;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
        this.blogTagService = blogTagService;
        this.userFollowRepository = userFollowRepository;
        this.pageFollowRepository = pageFollowRepository;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId) {
        Blog savedBlog = createBlogEntity(blogCreateDTO, actorUserId);
        return toBlogResponseDTO(savedBlog, actorUserId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogResponseDTO createModeratedBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId) {
        Blog savedBlog = createBlogEntity(blogCreateDTO, actorUserId);
        Blog moderatedBlog = aiBlogModerationService.moderateBlog(savedBlog);
        return toBlogResponseDTO(moderatedBlog, actorUserId);
    }

    private Blog createBlogEntity(BlogCreateDTO blogCreateDTO, UUID actorUserId) {
        User author = userValidator.validateUserExists(actorUserId);
        userValidator.validateUserActive(author);
        CafePage page = null;
        if (blogCreateDTO.getPageId() != null) {
            page = cafePageValidator.validateUserCanCreateBlogOnPage(blogCreateDTO.getPageId(), author.getUserId());
        }

        Blog blog = blogMapper.toBlog(blogCreateDTO);
        blog.setAuthor(author);
        blog.setPage(page);
        if (blogCreateDTO.getRegionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog region is required");
        }
        Region region = regionService.resolveExistingRegion(
                blogCreateDTO.getRegionId(),
                RegionRequirement.BLOG_LOCATION);
        blog.setRegionId(region.getRegionId());
        if (blogCreateDTO.getIsPinned() != null) {
            blog.setIsPinned(blogCreateDTO.getIsPinned());
        }
        if (blogCreateDTO.getAllowComment() != null) {
            blog.setAllowComment(blogCreateDTO.getAllowComment());
        }

        Blog savedBlog = blogRepository.save(blog);
        blogTagService.syncBlogTags(savedBlog, actorUserId, blogCreateDTO.getTaggedUserIds());
        return savedBlog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return getAllBlogs(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs(UUID viewerUserId) {
        return toBlogResponseDTOs(blogRepository.findAll(), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId) {
        return getAllBlogsByUserId(authorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, key = "'posts:' + #p0 + ':anon'")
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId) {
        return getAllBlogsByUserId(userId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'posts:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId) {
        return getAllBlogsByUserId(userId, viewerUserId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'posts:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1) + ':' + (#p2 == null ? 'all' : #p2)")
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId, PostStatus status) {
        userValidator.validateUserExists(userId);

        boolean isOwner = viewerUserId != null && viewerUserId.equals(userId);
        if (status != null && status != PostStatus.PUBLISHED && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can view non-published blogs");
        }

        List<Blog> blogs = status != null
                ? blogRepository.findByAuthorUserIdAndStatus(userId, status)
                : blogRepository.findByAuthorUserId(userId);

        if (status == null && !isOwner) {
            blogs = blogs.stream()
                    .filter(b -> b.getStatus() == PostStatus.PUBLISHED)
                    .collect(Collectors.toList());
        }

        return toBlogResponseDTOs(blogs, viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'saved:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public List<BlogResponseDTO> getSavedBlogsByUserId(UUID userId, UUID viewerUserId) {
        userValidator.validateUserExists(userId);
        return toBlogResponseDTOs(blogRepository.findSavedBlogsByUserId(userId), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'shared:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public List<BlogResponseDTO> getSharedBlogsByUserId(UUID userId, UUID viewerUserId) {
        userValidator.validateUserExists(userId);
        return toBlogResponseDTOs(distinctByBlogId(blogRepository.findSharedBlogsByUserId(userId)), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'shared:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1) + ':' + (#p2 == null ? 'recent' : #p2)")
    public List<BlogResponseDTO> getSharedBlogsByUserId(UUID userId, UUID viewerUserId, String sort) {
        userValidator.validateUserExists(userId);
        List<Blog> blogs = isShareCountSort(sort)
                ? blogRepository.findSharedBlogsByUserIdOrderByShareCount(userId)
                : blogRepository.findSharedBlogsByUserId(userId);
        return toBlogResponseDTOs(distinctByBlogId(blogs), viewerUserId);
    }

    private boolean isShareCountSort(String sort) {
        return sort != null
                && List.of("shareCount", "share_count", "shares").contains(sort.trim());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE,
            key = "'tagged:' + #p0 + ':' + (#p1 == null ? 'anon' : #p1)")
    public List<BlogResponseDTO> getTaggedBlogsByUserId(UUID userId, UUID viewerUserId) {
        userValidator.validateUserExists(userId);
        return toBlogResponseDTOs(blogRepository.findTaggedBlogsByUserId(userId), viewerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0")
    public BlogResponseDTO getBlogById(UUID blogId) {
        return getBlogById(blogId, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0", condition = "#p1 == null")
    public BlogResponseDTO getBlogById(UUID blogId, UUID viewerUserId) {
        return toBlogResponseDTO(blogValidator.validateBlogExists(blogId), viewerUserId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogResponseDTO updateBlog(UUID blogId, UUID actorUserId, BlogUpdateDTO blogUpdateDTO) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        validateBlogOwner(blog, actorUserId);

        if (blogUpdateDTO.getPageId() != null) {
            CafePage page = cafePageValidator.validateUserCanCreateBlogOnPage(blogUpdateDTO.getPageId(), actorUserId);
            blog.setPage(page);
        }
        if (blogUpdateDTO.getRegionId() != null) {
            Region region = regionService.resolveExistingRegion(
                    blogUpdateDTO.getRegionId(),
                    RegionRequirement.BLOG_LOCATION);
            blog.setRegionId(region.getRegionId());
        }
        if (blogUpdateDTO.getContent() != null) {
            blog.setContent(blogUpdateDTO.getContent());
        }
        if (blogUpdateDTO.getImageUrls() != null) {
            blog.setImageUrls(blogUpdateDTO.getImageUrls());
        }
        if (blogUpdateDTO.getStatus() != null) {
            blog.setStatus(blogUpdateDTO.getStatus());
        }
        if (blogUpdateDTO.getIsPinned() != null) {
            blog.setIsPinned(blogUpdateDTO.getIsPinned());
        }
        if (blogUpdateDTO.getAllowComment() != null) {
            blog.setAllowComment(blogUpdateDTO.getAllowComment());
        }

        Blog updatedBlog = blogRepository.save(blog);
        if (blogUpdateDTO.getTaggedUserIds() != null) {
            blogTagService.syncBlogTags(updatedBlog, actorUserId, blogUpdateDTO.getTaggedUserIds());
        }
        return toBlogResponseDTO(updatedBlog, actorUserId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public void deleteBlog(UUID blogId, UUID actorUserId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        validateBlogOwner(blog, actorUserId);
        blogTagService.deleteBlogTags(blogId);
        blogRepository.delete(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogTaggedUserResponseDTO> getTagSuggestions(UUID actorUserId, String keyword) {
        return blogTagService.getTagSuggestions(actorUserId, keyword);
    }

    private void validateBlogOwner(Blog blog, UUID actorUserId) {
        if (actorUserId == null || blog.getAuthor() == null || !actorUserId.equals(blog.getAuthor().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to manage this blog");
        }
    }

    private BlogResponseDTO toBlogResponseDTO(Blog blog, UUID viewerUserId) {
        BlogResponseDTO response = blogMapper.toBlogResponseDTO(blog);
        UUID blogId = blog.getId();
        if (blog.getRegionId() != null) {
            regionRepository.findById(blog.getRegionId())
                    .ifPresent(region -> {
                        response.setRegionCity(region.getCity());
                        response.setRegionProvince(region.getProvince());
                    });
        }
        response.setIsLike(viewerUserId != null && blogLikeRepository.existsByUserUserIdAndBlogId(viewerUserId, blogId));
        response.setIsSave(viewerUserId != null && blogSaveRepository.findByUserUserIdAndBlogId(viewerUserId, blogId).isPresent());
        UUID authorId = blog.getAuthor() != null ? blog.getAuthor().getUserId() : null;
        response.setIsAuthorFollowing(viewerUserId != null && authorId != null
                && userFollowRepository.existsByFollowerUserIdAndFollowingUserId(viewerUserId, authorId));
        UUID blogPageId = blog.getPageId();
        response.setIsPageFollowing(viewerUserId != null && blogPageId != null
                && pageFollowRepository.existsByUserUserIdAndCafePageId(viewerUserId, blogPageId));
        response.setSaveCount(blogSaveRepository.countByBlogId(blogId));

        response.setRatingScore(resolveRatingScore(blogId));
        response.setRatingCount(blogRatingRepository.countByBlogId(blogId));
        response.setTaggedUsers(blogTagService.getTaggedUsers(blogId));
        if (viewerUserId == null) {
            response.setIsRating(false);
            response.setMyRating(null);
            return response;
        }

        blogRatingRepository.findByUserUserIdAndBlogId(viewerUserId, blogId)
                .ifPresentOrElse(
                        rating -> {
                            response.setIsRating(true);
                            response.setMyRating(rating.getRating());
                        },
                        () -> {
                            response.setIsRating(false);
                            response.setMyRating(null);
                        });
        return response;
    }

    private List<BlogResponseDTO> toBlogResponseDTOs(List<Blog> blogs, UUID viewerUserId) {
        List<Blog> safeBlogs = safeList(blogs).stream()
                .filter(Objects::nonNull)
                .filter(blog -> blog.getId() != null)
                .toList();

        if (safeBlogs.isEmpty()) {
            return List.of();
        }

        List<UUID> blogIds = safeBlogs.stream()
                .map(Blog::getId)
                .toList();
        Map<UUID, List<String>> imageUrlsByBlogId = loadImageUrlsByBlogId(blogIds);
        Map<UUID, Region> regionsById = loadRegionsById(safeBlogs);
        Set<UUID> likedBlogIds = loadLikedBlogIds(viewerUserId, blogIds);
        Set<UUID> savedBlogIds = loadSavedBlogIds(viewerUserId, blogIds);
        Map<UUID, Long> saveCountsByBlogId = loadSaveCountsByBlogId(blogIds);
        Map<UUID, BlogRatingRepository.BlogRatingSummaryRow> ratingSummariesByBlogId = loadRatingSummariesByBlogId(blogIds);
        Map<UUID, Integer> myRatingsByBlogId = loadMyRatingsByBlogId(viewerUserId, blogIds);
        Map<UUID, List<BlogTaggedUserResponseDTO>> taggedUsersByBlogId = loadTaggedUsersByBlogId(blogIds);

        List<BlogResponseDTO> responses = new ArrayList<>(safeBlogs.size());
        for (Blog blog : safeBlogs) {
            UUID blogId = blog.getId();
            BlogResponseDTO response = toBlogListResponseDTO(blog);
            response.setImageUrls(imageUrlsByBlogId.getOrDefault(blogId, List.of()));

            Region region = regionsById.get(blog.getRegionId());
            if (region != null) {
                response.setRegionCity(region.getCity());
                response.setRegionProvince(region.getProvince());
            }

            response.setIsLike(likedBlogIds.contains(blogId));
            response.setIsSave(savedBlogIds.contains(blogId));
            response.setSaveCount(saveCountsByBlogId.getOrDefault(blogId, 0L));

            BlogRatingRepository.BlogRatingSummaryRow ratingSummary = ratingSummariesByBlogId.get(blogId);
            response.setRatingScore(ratingSummary == null || ratingSummary.getAverageRating() == null
                    ? 0.0
                    : ratingSummary.getAverageRating());
            response.setRatingCount(ratingSummary == null || ratingSummary.getRatingCount() == null
                    ? 0L
                    : ratingSummary.getRatingCount());

            Integer myRating = myRatingsByBlogId.get(blogId);
            response.setIsRating(myRating != null);
            response.setMyRating(myRating);
            response.setTaggedUsers(taggedUsersByBlogId.getOrDefault(blogId, List.of()));
            responses.add(response);
        }

        return responses;
    }

    private BlogResponseDTO toBlogListResponseDTO(Blog blog) {
        BlogResponseDTO response = new BlogResponseDTO();
        response.setId(blog.getId());
        response.setRegionId(blog.getRegionId());
        response.setContent(blog.getContent());
        response.setStatus(blog.getStatus());
        response.setIsPinned(blog.getIsPinned());
        response.setAllowComment(blog.getAllowComment());
        response.setLikeCount(blog.getLikeCount());
        response.setShareCount(blog.getShareCount());
        response.setCommentCount(blog.getCommentCount());
        response.setCreatedAt(blog.getCreatedAt());
        response.setUpdatedAt(blog.getUpdatedAt());

        User author = blog.getAuthor();
        if (author != null) {
            response.setAuthorUserId(author.getUserId());
            response.setAuthorUserName(author.getUserName());
            response.setAuthorUserFullName(author.getUserFullName());
            response.setAuthorUserAvatar(author.getUserAvatar());
        }

        CafePage page = blog.getPage();
        if (page != null) {
            response.setPageId(page.getId());
            response.setPageName(page.getName());
            response.setPageAvatarUrl(page.getAvatarUrl());
            response.setDisplayAuthorType(BlogDisplayAuthorType.CAFE_PAGE);
            response.setDisplayName(page.getName());
            response.setDisplayAvatarUrl(page.getAvatarUrl());
        } else {
            response.setPageId(blog.getPageId());
            response.setDisplayAuthorType(BlogDisplayAuthorType.USER);
            response.setDisplayName(firstNonBlank(
                    author == null ? null : author.getUserName(),
                    author == null ? null : author.getUserFullName()));
            response.setDisplayAvatarUrl(author == null ? null : author.getUserAvatar());
        }

        return response;
    }

    private Map<UUID, List<String>> loadImageUrlsByBlogId(List<UUID> blogIds) {
        Map<UUID, List<String>> imageUrlsByBlogId = new LinkedHashMap<>();
        for (BlogRepository.BlogImageUrlRow row : safeList(blogRepository.findImageUrlsByBlogIds(blogIds))) {
            if (row.getBlogId() != null && row.getImageUrl() != null) {
                imageUrlsByBlogId.computeIfAbsent(row.getBlogId(), ignored -> new ArrayList<>()).add(row.getImageUrl());
            }
        }
        return imageUrlsByBlogId;
    }

    private Map<UUID, Region> loadRegionsById(List<Blog> blogs) {
        List<UUID> regionIds = blogs.stream()
                .map(Blog::getRegionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, Region> regionsById = new LinkedHashMap<>();
        for (Region region : safeList(regionRepository.findAllById(regionIds))) {
            if (region.getRegionId() != null) {
                regionsById.put(region.getRegionId(), region);
            }
        }
        return regionsById;
    }

    private Set<UUID> loadLikedBlogIds(UUID viewerUserId, List<UUID> blogIds) {
        if (viewerUserId == null) {
            return Set.of();
        }
        return new HashSet<>(safeList(blogLikeRepository.findLikedBlogIdsByUserIdAndBlogIds(viewerUserId, blogIds)));
    }

    private Set<UUID> loadSavedBlogIds(UUID viewerUserId, List<UUID> blogIds) {
        if (viewerUserId == null) {
            return Set.of();
        }
        return new HashSet<>(safeList(blogSaveRepository.findSavedBlogIdsByUserIdAndBlogIds(viewerUserId, blogIds)));
    }

    private Map<UUID, Long> loadSaveCountsByBlogId(List<UUID> blogIds) {
        return safeList(blogSaveRepository.countSavesByBlogIds(blogIds))
                .stream()
                .filter(row -> row.getBlogId() != null)
                .collect(Collectors.toMap(
                        BlogSaveRepository.BlogCountRow::getBlogId,
                        row -> row.getCount() == null ? 0L : row.getCount(),
                        (first, ignored) -> first,
                        LinkedHashMap::new));
    }

    private Map<UUID, BlogRatingRepository.BlogRatingSummaryRow> loadRatingSummariesByBlogId(List<UUID> blogIds) {
        return safeList(blogRatingRepository.findRatingSummariesByBlogIds(blogIds))
                .stream()
                .filter(row -> row.getBlogId() != null)
                .collect(Collectors.toMap(
                        BlogRatingRepository.BlogRatingSummaryRow::getBlogId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new));
    }

    private Map<UUID, Integer> loadMyRatingsByBlogId(UUID viewerUserId, List<UUID> blogIds) {
        if (viewerUserId == null) {
            return Collections.emptyMap();
        }

        return safeList(blogRatingRepository.findUserRatingsByUserIdAndBlogIds(viewerUserId, blogIds))
                .stream()
                .filter(row -> row.getBlogId() != null)
                .collect(Collectors.toMap(
                        BlogRatingRepository.BlogUserRatingRow::getBlogId,
                        BlogRatingRepository.BlogUserRatingRow::getRating,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
    }

    private Map<UUID, List<BlogTaggedUserResponseDTO>> loadTaggedUsersByBlogId(List<UUID> blogIds) {
        Map<UUID, List<BlogTaggedUserResponseDTO>> taggedUsersByBlogId = new LinkedHashMap<>();
        for (BlogTaggedUser tag : safeList(blogTaggedUserRepository.findByBlogIdInWithTaggedUser(blogIds))) {
            if (tag.getBlog() == null || tag.getBlog().getId() == null || tag.getTaggedUser() == null) {
                continue;
            }
            taggedUsersByBlogId
                    .computeIfAbsent(tag.getBlog().getId(), ignored -> new ArrayList<>())
                    .add(toTaggedUserResponseDTO(tag.getTaggedUser()));
        }
        return taggedUsersByBlogId;
    }

    private BlogTaggedUserResponseDTO toTaggedUserResponseDTO(User taggedUser) {
        BlogTaggedUserResponseDTO response = new BlogTaggedUserResponseDTO();
        response.setId(taggedUser.getUserId());
        response.setUserName(taggedUser.getUserName());
        return response;
    }

    private <T> List<T> safeList(Iterable<T> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        values.forEach(result::add);
        return result;
    }

    private String firstNonBlank(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }

    private List<Blog> distinctByBlogId(List<Blog> blogs) {
        LinkedHashMap<UUID, Blog> distinctBlogs = new LinkedHashMap<>();

        for (Blog blog : blogs) {
            if (blog.getId() != null) {
                distinctBlogs.putIfAbsent(blog.getId(), blog);
            }
        }

        return distinctBlogs.values().stream().toList();
    }

    private double resolveRatingScore(UUID blogId) {
        Double ratingScore = blogRatingRepository.findAverageRatingByBlogId(blogId);
        return ratingScore == null ? 0.0 : ratingScore;
    }
}
