package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogEventResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.service.serviceInterface.BlogEventService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BlogEventServiceImpl implements BlogEventService {

    private final BlogEventRepository blogEventRepository;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;

    public BlogEventServiceImpl(
            BlogEventRepository blogEventRepository,
            BlogValidator blogValidator,
            UserValidator userValidator) {
        this.blogEventRepository = blogEventRepository;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public BlogEventResponse recordEvent(UUID blogId, UUID userId, BlogEventType eventType, Double weight) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        User user = null;
        if (userId != null) {
            user = userValidator.validateUserExists(userId);
            userValidator.validateUserActive(user);
        }

        BlogEvent blogEvent = new BlogEvent();
        blogEvent.setBlog(blog);
        blogEvent.setUser(user);
        blogEvent.setEventType(eventType);
        blogEvent.setWeight(weight == null ? defaultWeight(eventType) : weight);

        return toResponse(blogEventRepository.save(blogEvent));
    }

    private BlogEventResponse toResponse(BlogEvent blogEvent) {
        BlogEventResponse response = new BlogEventResponse();
        response.setId(blogEvent.getId());
        response.setBlogId(blogEvent.getBlog().getId());
        response.setUserId(blogEvent.getUser() == null ? null : blogEvent.getUser().getUserId());
        response.setEventType(blogEvent.getEventType());
        response.setWeight(blogEvent.getWeight());
        response.setCreatedAt(blogEvent.getCreatedAt());
        return response;
    }

    private double defaultWeight(BlogEventType eventType) {
        return switch (eventType) {
            case VIEW -> 0.2;
            case LIKE -> 2.0;
            case COMMENT -> 4.0;
            case SHARE -> 6.0;
            case SAVE -> 5.0;
            case REPORT -> -10.0;
        };
    }
}
