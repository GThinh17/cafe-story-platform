package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageMember;
import com.cafestory.entity.PageMemberId;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageMemberStatus;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PageMemberRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.service.serviceInterface.CafePageService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CafePageServiceImpl implements CafePageService {
    private static final int DEFAULT_BLOG_PAGE_SIZE = 20;
    private static final int MAX_BLOG_PAGE_SIZE = 50;
    private static final int CURSOR_VERSION = 1;
    private static final ObjectMapper CURSOR_OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final CafePageRepository cafePageRepository;
    private final BlogRepository blogRepository;
    private final PageMemberRepository pageMemberRepository;
    private final RegionRepository regionRepository;
    private final CafePageMapper cafePageMapper;
    private final BlogMapper blogMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public CafePageServiceImpl(
            CafePageRepository cafePageRepository,
            BlogRepository blogRepository,
            PageMemberRepository pageMemberRepository,
            RegionRepository regionRepository,
            CafePageMapper cafePageMapper,
            BlogMapper blogMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.cafePageRepository = cafePageRepository;
        this.blogRepository = blogRepository;
        this.pageMemberRepository = pageMemberRepository;
        this.regionRepository = regionRepository;
        this.cafePageMapper = cafePageMapper;
        this.blogMapper = blogMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public CafePageResponseDTO createCafePage(CafePageCreateDTO cafePageCreateDTO) {
        User owner = userValidator.validateUserExists(cafePageCreateDTO.getOwnerUserId());
        userValidator.validateUserActive(owner);
        cafePageValidator.validateUserCanCreateCafePage(owner.getUserId());

        CafePage cafePage = cafePageMapper.toCafePage(cafePageCreateDTO);
        cafePage.setOwner(owner);
        if (cafePageCreateDTO.getRegionId() != null) {
            cafePage.setRegion(regionRepository.findById(cafePageCreateDTO.getRegionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found")));
        }

        CafePage savedCafePage = cafePageRepository.save(cafePage);
        pageMemberRepository.saveAll(createPageMembers(savedCafePage, owner, cafePageCreateDTO.getCoOwnerUserIds()));
        return cafePageMapper.toCafePageResponseDTO(savedCafePage);
    }

    private List<PageMember> createPageMembers(CafePage cafePage, User owner, List<UUID> coOwnerUserIds) {
        Set<UUID> memberUserIds = new LinkedHashSet<>();
        memberUserIds.add(owner.getUserId());
        if (coOwnerUserIds != null) {
            memberUserIds.addAll(coOwnerUserIds);
        }

        return memberUserIds.stream()
                .map(userId -> createPageMember(cafePage, owner, userId))
                .toList();
    }

    private PageMember createPageMember(CafePage cafePage, User owner, UUID userId) {
        User memberUser = owner;
        if (!owner.getUserId().equals(userId)) {
            memberUser = userValidator.validateUserExists(userId);
            userValidator.validateUserActive(memberUser);
        }

        PageMember pageMember = new PageMember();
        pageMember.setId(new PageMemberId(cafePage.getId(), memberUser.getUserId()));
        pageMember.setCafePage(cafePage);
        pageMember.setUser(memberUser);
        pageMember.setRoleName(owner.getUserId().equals(memberUser.getUserId())
                ? PageMember.ROLE_OWNER
                : PageMember.ROLE_CO_OWNER);
        pageMember.setStatus(PageMemberStatus.ACTIVE);
        return pageMember;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getAllCafePages() {
        return cafePageRepository.findAll()
                .stream()
                .map(cafePageMapper::toCafePageResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId) {
        userValidator.validateUserExists(ownerUserId);
        return cafePageRepository.findByOwnerUserId(ownerUserId)
                .stream()
                .map(cafePageMapper::toCafePageResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CafePageResponseDTO getCafePageById(UUID cafePageId) {
        return cafePageMapper.toCafePageResponseDTO(cafePageValidator.validateCafePageExists(cafePageId));
    }

    @Override
    @Transactional(readOnly = true)
    public BlogCursorPageResponseDTO getBlogsByCafePageId(UUID cafePageId, String cursor, int size) {
        cafePageValidator.validateCafePageExists(cafePageId);
        int safeSize = normalizeBlogPageSize(size);
        CafePageBlogCursor pageCursor = decodeCursor(cursor);
        List<Blog> blogs = findCafePageBlogs(cafePageId, pageCursor, safeSize + 1);
        boolean hasMore = blogs.size() > safeSize;
        List<Blog> pageItems = hasMore ? blogs.subList(0, safeSize) : blogs;
        List<BlogResponseDTO> items = pageItems
                .stream()
                .map(blogMapper::toBlogResponseDTO)
                .toList();

        BlogCursorPageResponseDTO response = new BlogCursorPageResponseDTO();
        response.setItems(items);
        response.setHasMore(hasMore);
        response.setNextCursor(hasMore && !pageItems.isEmpty()
                ? encodeCursor(pageItems.getLast())
                : null);
        return response;
    }

    @Override
    @Transactional
    public CafePageResponseDTO updateCafePage(UUID cafePageId, UUID actorUserId, CafePageUpdateDTO cafePageUpdateDTO) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);

        if (cafePageUpdateDTO.getRegionId() != null) {
            cafePage.setRegion(regionRepository.findById(cafePageUpdateDTO.getRegionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Region not found")));
        }
        if (cafePageUpdateDTO.getName() != null) {
            cafePage.setName(cafePageUpdateDTO.getName());
        }
        if (cafePageUpdateDTO.getAddress() != null) {
            cafePage.setAddress(cafePageUpdateDTO.getAddress());
        }
        if (cafePageUpdateDTO.getDescription() != null) {
            cafePage.setDescription(cafePageUpdateDTO.getDescription());
        }
        if (cafePageUpdateDTO.getAvatarUrl() != null) {
            cafePage.setAvatarUrl(cafePageUpdateDTO.getAvatarUrl());
        }
        if (cafePageUpdateDTO.getCoverUrl() != null) {
            cafePage.setCoverUrl(cafePageUpdateDTO.getCoverUrl());
        }
        if (cafePageUpdateDTO.getStatus() != null) {
            cafePage.setStatus(cafePageUpdateDTO.getStatus());
        }

        CafePage updatedCafePage = cafePageRepository.save(cafePage);
        return cafePageMapper.toCafePageResponseDTO(updatedCafePage);
    }

    @Override
    @Transactional
    public void deleteCafePage(UUID cafePageId, UUID actorUserId) {
        cafePageValidator.validateUserCanManagePage(cafePageId, actorUserId);
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        cafePageRepository.delete(cafePage);
    }

    private int normalizeBlogPageSize(int size) {
        if (size <= 0) {
            return DEFAULT_BLOG_PAGE_SIZE;
        }
        return Math.min(size, MAX_BLOG_PAGE_SIZE);
    }

    private List<Blog> findCafePageBlogs(UUID cafePageId, CafePageBlogCursor cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (cursor == null) {
            return blogRepository.findPublishedCafePageBlogsFirstPage(cafePageId, pageRequest);
        }

        return blogRepository.findPublishedCafePageBlogsAfterCursor(
                cafePageId,
                cursor.afterCreatedAt(),
                cursor.afterId(),
                pageRequest);
    }

    private CafePageBlogCursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            CafePageBlogCursorPayload payload = CURSOR_OBJECT_MAPPER.readValue(json, CafePageBlogCursorPayload.class);
            if (payload.version() != CURSOR_VERSION || payload.afterCreatedAt() == null || payload.afterId() == null) {
                throw invalidCursor();
            }
            return new CafePageBlogCursor(LocalDateTime.parse(payload.afterCreatedAt()), payload.afterId());
        } catch (IllegalArgumentException | JsonProcessingException | DateTimeParseException error) {
            throw invalidCursor();
        }
    }

    private String encodeCursor(Blog blog) {
        try {
            CafePageBlogCursorPayload payload = new CafePageBlogCursorPayload(
                    blog.getCreatedAt().toString(),
                    blog.getId(),
                    CURSOR_VERSION);
            String json = CURSOR_OBJECT_MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException error) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create blog cursor");
        }
    }

    private ResponseStatusException invalidCursor() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cafe page blog cursor");
    }

    private record CafePageBlogCursor(LocalDateTime afterCreatedAt, UUID afterId) {
    }

    private record CafePageBlogCursorPayload(String afterCreatedAt, UUID afterId, int version) {
    }
}
