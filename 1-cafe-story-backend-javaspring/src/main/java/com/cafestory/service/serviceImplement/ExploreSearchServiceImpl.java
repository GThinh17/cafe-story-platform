package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ExploreBlogSearchResultResponseDTO;
import com.cafestory.dto.responseDTO.ExploreCafePageSearchResultResponseDTO;
import com.cafestory.dto.responseDTO.ExploreSearchResponseDTO;
import com.cafestory.dto.responseDTO.ExploreUserSearchResultResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Region;
import com.cafestory.entity.User;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.ExploreSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExploreSearchServiceImpl implements ExploreSearchService {

    static final int MAX_RESULT_SIZE = 20;

    private final UserRepository userRepository;
    private final CafePageRepository cafePageRepository;
    private final BlogRepository blogRepository;

    public ExploreSearchServiceImpl(
            UserRepository userRepository,
            CafePageRepository cafePageRepository,
            BlogRepository blogRepository) {
        this.userRepository = userRepository;
        this.cafePageRepository = cafePageRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ExploreSearchResponseDTO search(String query, int size) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.length() < 2) {
            return emptyResponse();
        }

        int boundedSize = Math.max(1, Math.min(size, MAX_RESULT_SIZE));
        PageRequest pageRequest = PageRequest.of(0, boundedSize);

        ExploreSearchResponseDTO response = new ExploreSearchResponseDTO();
        response.setUsers(userRepository.searchActiveUsers(normalizedQuery, pageRequest)
                .stream()
                .map(this::toUserResult)
                .toList());
        response.setCafePages(cafePageRepository.searchActiveCafePages(normalizedQuery, pageRequest)
                .stream()
                .map(this::toCafePageResult)
                .toList());
        response.setBlogs(blogRepository.searchPublishedBlogs(normalizedQuery, pageRequest)
                .stream()
                .map(this::toBlogResult)
                .toList());
        return response;
    }

    private ExploreSearchResponseDTO emptyResponse() {
        ExploreSearchResponseDTO response = new ExploreSearchResponseDTO();
        response.setUsers(List.of());
        response.setCafePages(List.of());
        response.setBlogs(List.of());
        return response;
    }

    private ExploreUserSearchResultResponseDTO toUserResult(User user) {
        Region region = user.getRegion();
        ExploreUserSearchResultResponseDTO response = new ExploreUserSearchResultResponseDTO();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setUserFullName(user.getUserFullName());
        response.setUserAvatar(user.getUserAvatar());
        response.setRegionCity(region == null ? null : region.getCity());
        return response;
    }

    private ExploreCafePageSearchResultResponseDTO toCafePageResult(CafePage cafePage) {
        Region region = cafePage.getRegion();
        ExploreCafePageSearchResultResponseDTO response = new ExploreCafePageSearchResultResponseDTO();
        response.setId(cafePage.getId());
        response.setName(cafePage.getName());
        response.setAvatarUrl(cafePage.getAvatarUrl());
        response.setAddress(cafePage.getAddress());
        response.setRegionCity(region == null ? null : region.getCity());
        return response;
    }

    private ExploreBlogSearchResultResponseDTO toBlogResult(Blog blog) {
        User author = blog.getAuthor();
        CafePage page = blog.getPage();
        ExploreBlogSearchResultResponseDTO response = new ExploreBlogSearchResultResponseDTO();
        response.setId(blog.getId());
        response.setContent(blog.getContent());
        response.setCreatedAt(blog.getCreatedAt());
        response.setAuthorUserId(author == null ? null : author.getUserId());
        response.setAuthorUserName(author == null ? null : author.getUserName());
        response.setAuthorUserFullName(author == null ? null : author.getUserFullName());
        response.setAuthorUserAvatar(author == null ? null : author.getUserAvatar());
        response.setPageId(page == null ? null : page.getId());
        response.setPageName(page == null ? null : page.getName());
        response.setPageAvatarUrl(page == null ? null : page.getAvatarUrl());
        response.setDisplayName(page == null ? response.getAuthorUserName() : page.getName());
        response.setDisplayAvatarUrl(page == null ? response.getAuthorUserAvatar() : page.getAvatarUrl());
        return response;
    }
}
