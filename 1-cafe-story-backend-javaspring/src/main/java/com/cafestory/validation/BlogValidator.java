package com.cafestory.validation;

import com.cafestory.entity.Blog;
import com.cafestory.repository.BlogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class BlogValidator {

    private final BlogRepository blogRepository;

    public BlogValidator(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public Blog validateBlogExists(UUID blogId) {
        validateBlogIdRequired(blogId);
        return blogRepository.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    }

    private void validateBlogIdRequired(UUID blogId) {
        if (blogId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog id is required");
        }
    }
}
