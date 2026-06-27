package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.requestDTO.FeedImpressionItemRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.FeedImpression;
import com.cafestory.entity.User;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.FeedImpressionRepository;
import com.cafestory.service.serviceInterface.FeedImpressionService;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FeedImpressionServiceImpl implements FeedImpressionService {

    private final FeedImpressionRepository feedImpressionRepository;
    private final BlogRepository blogRepository;
    private final UserValidator userValidator;

    public FeedImpressionServiceImpl(
            FeedImpressionRepository feedImpressionRepository,
            BlogRepository blogRepository,
            UserValidator userValidator) {
        this.feedImpressionRepository = feedImpressionRepository;
        this.blogRepository = blogRepository;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public FeedImpressionResponseDTO recordImpressions(UUID userId, FeedImpressionBatchRequestDTO request) {
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        List<UUID> blogIds = request.getItems()
                .stream()
                .map(FeedImpressionItemRequestDTO::getBlogId)
                .distinct()
                .toList();
        Map<UUID, Blog> blogsById = blogRepository.findAllById(blogIds)
                .stream()
                .collect(Collectors.toMap(Blog::getId, Function.identity()));

        blogIds.stream()
                .filter(blogId -> !blogsById.containsKey(blogId))
                .findFirst()
                .ifPresent(missingBlogId -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found");
                });

        List<FeedImpression> impressions = request.getItems()
                .stream()
                .map(item -> toFeedImpression(user, blogsById.get(item.getBlogId()), item.getPosition()))
                .toList();

        feedImpressionRepository.saveAll(impressions);

        FeedImpressionResponseDTO response = new FeedImpressionResponseDTO();
        response.setRecordedCount(impressions.size());
        return response;
    }

    private FeedImpression toFeedImpression(User user, Blog blog, Integer position) {
        FeedImpression impression = new FeedImpression();
        impression.setUser(user);
        impression.setBlog(blog);
        impression.setPosition(position);
        return impression;
    }
}
