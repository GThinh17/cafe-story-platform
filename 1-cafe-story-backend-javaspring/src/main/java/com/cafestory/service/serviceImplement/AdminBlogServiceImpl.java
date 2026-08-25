package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceInterface.AdminBlogService;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminBlogServiceImpl implements AdminBlogService {

    private static final Logger log = LoggerFactory.getLogger(AdminBlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final BlogTagService blogTagService;
    private final NotificationService notificationService;

    public AdminBlogServiceImpl(
            BlogRepository blogRepository,
            BlogMapper blogMapper,
            BlogTagService blogTagService,
            NotificationService notificationService) {
        this.blogRepository = blogRepository;
        this.blogMapper = blogMapper;
        this.blogTagService = blogTagService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponseDTO> getBlogs(PostStatus status, UUID authorUserId, UUID pageId, Pageable pageable) {
        Page<Blog> blogs = blogRepository.findAdminBlogs(status, authorUserId, pageId, pageable);
        Map<UUID, List<BlogTaggedUserResponseDTO>> taggedUsersByBlogId =
                blogTagService.getTaggedUsersByBlogIds(blogs.stream().map(Blog::getId).toList());
        return blogs.map(blog -> {
            BlogResponseDTO response = blogMapper.toBlogResponseDTO(blog);
            response.setTaggedUsers(taggedUsersByBlogId.getOrDefault(blog.getId(), List.of()));
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlog(UUID blogId) {
        return toBlogResponseDTO(findBlog(blogId));
    }

    @Override
    @Transactional
    public BlogResponseDTO updateBlogStatus(UUID blogId, AdminPostStatusUpdateRequestDTO request) {
        Blog blog = findBlog(blogId);
        PostStatus previousStatus = blog.getStatus();
        blog.setStatus(request.getStatus());
        Blog savedBlog = blogRepository.save(blog);
        // Chỉ báo khi trạng thái thực sự đổi — admin lưu lại cùng trạng thái
        // không phải một quyết định kiểm duyệt mới.
        if (previousStatus != request.getStatus()) {
            notifyAuthorOfModeration(savedBlog, request.getStatus(), request.getReason());
        }
        return toBlogResponseDTO(savedBlog);
    }

    /**
     * Báo cho tác giả khi admin duyệt tay.
     *
     * <p>Trước đây chỉ {@code AiBlogModerationServiceImpl} gửi thông báo kiểm
     * duyệt, nên bài bị AI đẩy sang admin rồi admin xử tay thì tác giả không nhận
     * được gì — kể cả khi reload.
     *
     * <p>Nuốt lỗi giống đường AI: thông báo hỏng không được phép làm rollback
     * quyết định kiểm duyệt của admin.
     */
    private void notifyAuthorOfModeration(Blog blog, PostStatus status, String reason) {
        String moderationStatus = switch (status) {
            case PUBLISHED -> "APPROVED";
            case HIDDEN, REMOVED -> "DENIED";
            case DRAFT -> null;
        };
        if (moderationStatus == null || blog.getAuthor() == null) {
            return;
        }
        try {
            notificationService.createModerationNotification(
                    blog.getAuthor().getUserId(),
                    blog.getId(),
                    moderationStatus,
                    reason == null || reason.isBlank() ? null : reason.trim());
        } catch (Exception exception) {
            log.error("Không gửi được thông báo kiểm duyệt cho blogId={}", blog.getId(), exception);
        }
    }

    @Override
    @Transactional
    public void deleteBlog(UUID blogId) {
        Blog blog = findBlog(blogId);
        blogTagService.deleteBlogTags(blogId);
        blogRepository.delete(blog);
    }

    private Blog findBlog(UUID blogId) {
        return blogRepository.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    }

    private BlogResponseDTO toBlogResponseDTO(Blog blog) {
        BlogResponseDTO response = blogMapper.toBlogResponseDTO(blog);
        response.setTaggedUsers(blogTagService.getTaggedUsers(blog.getId()));
        return response;
    }
}
