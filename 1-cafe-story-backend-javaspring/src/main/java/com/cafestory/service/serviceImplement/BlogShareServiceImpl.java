package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ShareType;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.service.serviceInterface.BlogShareService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogShareServiceImpl implements BlogShareService {

    private final BlogShareRepository blogShareRepository;
    private final BlogInteractionMapper blogInteractionMapper;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;

    public BlogShareServiceImpl(
            BlogShareRepository blogShareRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            UserValidator userValidator) {
        this.blogShareRepository = blogShareRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public BlogShareResponseDTO shareBlog(UUID blogId, UUID userId, ShareType shareType) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);
        ShareType resolvedShareType = resolveShareType(shareType);
        validateShareTypeAllowed(blog, resolvedShareType);

        BlogShare blogShare = new BlogShare();
        blogShare.setBlog(blog);
        blogShare.setUser(user);
        blogShare.setShareType(resolvedShareType);

        BlogShare savedBlogShare = blogShareRepository.save(blogShare);
        incrementShareCount(blog);
        return blogInteractionMapper.toBlogShareResponseDTO(savedBlogShare);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogShareResponseDTO> getSharesByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        return blogShareRepository.findByBlogId(blogId)
                .stream()
                .map(blogInteractionMapper::toBlogShareResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogShareResponseDTO> getSharesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogShareRepository.findByUserUserId(userId)
                .stream()
                .map(blogInteractionMapper::toBlogShareResponseDTO)
                .toList();
    }

    private void incrementShareCount(Blog blog) {
        int currentCount = blog.getShareCount() == null ? 0 : blog.getShareCount();
        blog.setShareCount(currentCount + 1);
    }

    private ShareType resolveShareType(ShareType shareType) {
        return shareType == null ? ShareType.PUBLIC : shareType;
    }

    private void validateShareTypeAllowed(Blog blog, ShareType shareType) {
        if (shareType == ShareType.PAGE_ONLY && blog.getPageId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page only share requires a blog that belongs to a page");
        }
    }
}
