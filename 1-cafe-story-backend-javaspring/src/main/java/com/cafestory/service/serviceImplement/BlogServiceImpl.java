package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.RegionRequirement;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogSaveRepository blogSaveRepository;
    private final BlogRatingRepository blogRatingRepository;
    private final RegionRepository regionRepository;
    private final BlogMapper blogMapper;
    private final BlogValidator blogValidator;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;
    private final BlogTagService blogTagService;
    private final RegionService regionService;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            BlogLikeRepository blogLikeRepository,
            BlogSaveRepository blogSaveRepository,
            BlogRatingRepository blogRatingRepository,
            RegionRepository regionRepository,
            BlogMapper blogMapper,
            BlogValidator blogValidator,
            CafePageValidator cafePageValidator,
            UserValidator userValidator,
            BlogTagService blogTagService,
            RegionService regionService) {
        this.blogRepository = blogRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.blogSaveRepository = blogSaveRepository;
        this.blogRatingRepository = blogRatingRepository;
        this.regionRepository = regionRepository;
        this.blogMapper = blogMapper;
        this.blogValidator = blogValidator;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
        this.blogTagService = blogTagService;
        this.regionService = regionService;
    }

    @Override
    @Transactional
    public BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId) {
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
        return toBlogResponseDTO(savedBlog, actorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return getAllBlogs(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs(UUID viewerUserId) {
        return blogRepository.findAll()
                .stream()
                .map(blog -> toBlogResponseDTO(blog, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId) {
        return getAllBlogsByUserId(authorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId) {
        return getAllBlogsByUserId(userId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId) {
        userValidator.validateUserExists(userId);
        return blogRepository.findByAuthorUserId(userId)
                .stream()
                .map(blog -> toBlogResponseDTO(blog, viewerUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlogById(UUID blogId) {
        return getBlogById(blogId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlogById(UUID blogId, UUID viewerUserId) {
        return toBlogResponseDTO(blogValidator.validateBlogExists(blogId), viewerUserId);
    }

    @Override
    @Transactional
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

    private double resolveRatingScore(UUID blogId) {
        Double ratingScore = blogRatingRepository.findAverageRatingByBlogId(blogId);
        return ratingScore == null ? 0.0 : ratingScore;
    }
}
