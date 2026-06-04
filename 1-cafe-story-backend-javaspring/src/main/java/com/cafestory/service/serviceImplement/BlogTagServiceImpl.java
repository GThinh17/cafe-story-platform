package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogTaggedUser;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.User;
import com.cafestory.entity.UserFollow;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogTaggedUserMapper;
import com.cafestory.repository.BlogTaggedUserRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class BlogTagServiceImpl implements BlogTagService {

    private static final int MAX_TAGGED_USERS = 20;
    private static final int MAX_TAG_SUGGESTIONS = 20;

    private final BlogTaggedUserRepository blogTaggedUserRepository;
    private final UserFollowRepository userFollowRepository;
    private final PageFollowRepository pageFollowRepository;
    private final PageMemberRepository pageMemberRepository;
    private final UserValidator userValidator;
    private final BlogTaggedUserMapper blogTaggedUserMapper;
    private final NotificationService notificationService;

    public BlogTagServiceImpl(
            BlogTaggedUserRepository blogTaggedUserRepository,
            UserFollowRepository userFollowRepository,
            PageFollowRepository pageFollowRepository,
            PageMemberRepository pageMemberRepository,
            UserValidator userValidator,
            BlogTaggedUserMapper blogTaggedUserMapper,
            NotificationService notificationService) {
        this.blogTaggedUserRepository = blogTaggedUserRepository;
        this.userFollowRepository = userFollowRepository;
        this.pageFollowRepository = pageFollowRepository;
        this.pageMemberRepository = pageMemberRepository;
        this.userValidator = userValidator;
        this.blogTaggedUserMapper = blogTaggedUserMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public List<BlogTaggedUserResponseDTO> syncBlogTags(Blog blog, UUID actorUserId, List<UUID> taggedUserIds) {
        if (blog == null || blog.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog is required");
        }
        User actor = userValidator.validateUserExists(actorUserId);
        userValidator.validateUserActive(actor);

        List<UUID> requestedIds = normalizeTaggedUserIds(taggedUserIds);
        Map<UUID, BlogTaggedUser> existingTags = existingTagsByUserId(blog.getId());

        List<BlogTaggedUser> tagsToDelete = existingTags.entrySet()
                .stream()
                .filter(entry -> !requestedIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        blogTaggedUserRepository.deleteAll(tagsToDelete);

        List<BlogTaggedUser> tagsToCreate = new ArrayList<>();
        for (UUID taggedUserId : requestedIds) {
            User taggedUser = userValidator.validateUserExists(taggedUserId);
            userValidator.validateUserActive(taggedUser);
            validateTaggableUser(actorUserId, taggedUserId);
            if (!existingTags.containsKey(taggedUserId)) {
                BlogTaggedUser tag = new BlogTaggedUser();
                tag.setBlog(blog);
                tag.setTaggedUser(taggedUser);
                tag.setTaggedByUser(actor);
                tagsToCreate.add(tag);
            }
        }

        List<BlogTaggedUser> savedTags = blogTaggedUserRepository.saveAll(tagsToCreate);
        notifyNewTags(blog, actorUserId, savedTags);
        return getTaggedUsers(blog.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogTaggedUserResponseDTO> getTaggedUsers(UUID blogId) {
        return blogTaggedUserRepository.findByBlogIdOrderByCreatedAtAsc(blogId)
                .stream()
                .map(blogTaggedUserMapper::toBlogTaggedUserResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogTaggedUserResponseDTO> getTagSuggestions(UUID actorUserId, String keyword) {
        User actor = userValidator.validateUserExists(actorUserId);
        userValidator.validateUserActive(actor);

        String normalizedKeyword = normalizeKeyword(keyword);
        Map<UUID, User> candidates = new LinkedHashMap<>();
        candidates.put(actor.getUserId(), actor);

        for (UserFollow follow : userFollowRepository.findByFollowerUserId(actorUserId)) {
            addCandidate(candidates, follow.getFollowing());
        }

        for (PageFollow pageFollow : pageFollowRepository.findByUserUserId(actorUserId)) {
            addCandidate(candidates, pageFollow.getCafePage().getOwner());
            for (PageMember pageMember : pageMemberRepository.findByCafePageIdAndStatus(
                    pageFollow.getCafePage().getId(),
                    PageMemberStatus.ACTIVE)) {
                addCandidate(candidates, pageMember.getUser());
            }
        }

        return candidates.values()
                .stream()
                .filter(this::isActiveUser)
                .filter(user -> matchesKeyword(user, normalizedKeyword))
                .limit(MAX_TAG_SUGGESTIONS)
                .map(blogTaggedUserMapper::toBlogTaggedUserResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBlogTags(UUID blogId) {
        blogTaggedUserRepository.deleteByBlogId(blogId);
    }

    private List<UUID> normalizeTaggedUserIds(List<UUID> taggedUserIds) {
        List<UUID> normalizedIds = new ArrayList<>(new LinkedHashSet<>(taggedUserIds == null ? List.of() : taggedUserIds));
        if (normalizedIds.size() > MAX_TAGGED_USERS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A blog can tag at most 20 users");
        }
        return normalizedIds;
    }

    private Map<UUID, BlogTaggedUser> existingTagsByUserId(UUID blogId) {
        Map<UUID, BlogTaggedUser> tagsByUserId = new LinkedHashMap<>();
        for (BlogTaggedUser tag : blogTaggedUserRepository.findByBlogId(blogId)) {
            tagsByUserId.put(tag.getTaggedUser().getUserId(), tag);
        }
        return tagsByUserId;
    }

    private void validateTaggableUser(UUID actorUserId, UUID taggedUserId) {
        if (actorUserId.equals(taggedUserId)) {
            return;
        }
        if (userFollowRepository.existsByFollowerUserIdAndFollowingUserId(actorUserId, taggedUserId)) {
            return;
        }
        if (pageFollowRepository.existsTaggableUserFromFollowedPages(actorUserId, taggedUserId, PageMemberStatus.ACTIVE)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not eligible to be tagged");
    }

    private void notifyNewTags(Blog blog, UUID actorUserId, List<BlogTaggedUser> savedTags) {
        if (blog.getStatus() != PostStatus.PUBLISHED) {
            return;
        }
        for (BlogTaggedUser tag : savedTags) {
            notificationService.createTagNotification(tag.getTaggedUser().getUserId(), actorUserId, blog.getId());
        }
    }

    private void addCandidate(Map<UUID, User> candidates, User user) {
        if (user != null && user.getUserId() != null) {
            candidates.putIfAbsent(user.getUserId(), user);
        }
    }

    private boolean isActiveUser(User user) {
        return user != null && Boolean.TRUE.equals(user.getAccountStatus());
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesKeyword(User user, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return containsKeyword(user.getUserName(), keyword) || containsKeyword(user.getUserFullName(), keyword);
    }

    private boolean containsKeyword(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
