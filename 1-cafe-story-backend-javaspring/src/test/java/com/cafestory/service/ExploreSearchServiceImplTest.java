package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.ExploreSearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreSearchServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private ExploreSearchServiceImpl exploreSearchService;

    @Test
    void search_success_trimsQueryBoundsMaximumAndMapsResults_TC001() {
        Region region = region("Can Tho");
        User user = user(region);
        CafePage cafePage = cafePage(region);
        Blog blog = blog(user, cafePage);
        PageRequest boundedPage = PageRequest.of(0, 20);

        when(userRepository.searchActiveUsers("cafe", boundedPage)).thenReturn(List.of(user));
        when(cafePageRepository.searchActiveCafePages("cafe", boundedPage)).thenReturn(List.of(cafePage));
        when(blogRepository.searchPublishedBlogs("cafe", boundedPage)).thenReturn(List.of(blog));

        ExploreSearchResponseDTO result = exploreSearchService.search("  cafe  ", 99);

        ExploreUserSearchResultResponseDTO userResult = result.getUsers().getFirst();
        assertThat(userResult.getUserId()).isEqualTo(user.getUserId());
        assertThat(userResult.getUserName()).isEqualTo(user.getUserName());
        assertThat(userResult.getUserFullName()).isEqualTo(user.getUserFullName());
        assertThat(userResult.getUserAvatar()).isEqualTo(user.getUserAvatar());
        assertThat(userResult.getRegionCity()).isEqualTo("Can Tho");

        ExploreCafePageSearchResultResponseDTO cafeResult = result.getCafePages().getFirst();
        assertThat(cafeResult.getId()).isEqualTo(cafePage.getId());
        assertThat(cafeResult.getName()).isEqualTo(cafePage.getName());
        assertThat(cafeResult.getAvatarUrl()).isEqualTo(cafePage.getAvatarUrl());
        assertThat(cafeResult.getAddress()).isEqualTo(cafePage.getAddress());
        assertThat(cafeResult.getRegionCity()).isEqualTo("Can Tho");

        ExploreBlogSearchResultResponseDTO blogResult = result.getBlogs().getFirst();
        assertThat(blogResult.getId()).isEqualTo(blog.getId());
        assertThat(blogResult.getContent()).isEqualTo(blog.getContent());
        assertThat(blogResult.getCreatedAt()).isEqualTo(blog.getCreatedAt());
        assertThat(blogResult.getAuthorUserId()).isEqualTo(user.getUserId());
        assertThat(blogResult.getAuthorUserName()).isEqualTo(user.getUserName());
        assertThat(blogResult.getAuthorUserFullName()).isEqualTo(user.getUserFullName());
        assertThat(blogResult.getAuthorUserAvatar()).isEqualTo(user.getUserAvatar());
        assertThat(blogResult.getPageId()).isEqualTo(cafePage.getId());
        assertThat(blogResult.getPageName()).isEqualTo(cafePage.getName());
        assertThat(blogResult.getPageAvatarUrl()).isEqualTo(cafePage.getAvatarUrl());
        assertThat(blogResult.getDisplayName()).isEqualTo(cafePage.getName());
        assertThat(blogResult.getDisplayAvatarUrl()).isEqualTo(cafePage.getAvatarUrl());
    }

    @Test
    void search_success_boundsMinimumAndHandlesMissingRelations_TC002() {
        User user = user(null);
        CafePage cafePage = cafePage(null);
        Blog blog = blog(null, null);
        PageRequest boundedPage = PageRequest.of(0, 1);

        when(userRepository.searchActiveUsers("ab", boundedPage)).thenReturn(List.of(user));
        when(cafePageRepository.searchActiveCafePages("ab", boundedPage)).thenReturn(List.of(cafePage));
        when(blogRepository.searchPublishedBlogs("ab", boundedPage)).thenReturn(List.of(blog));

        ExploreSearchResponseDTO result = exploreSearchService.search("ab", 0);

        assertThat(result.getUsers().getFirst().getRegionCity()).isNull();
        assertThat(result.getCafePages().getFirst().getRegionCity()).isNull();
        assertThat(result.getBlogs().getFirst().getAuthorUserId()).isNull();
        assertThat(result.getBlogs().getFirst().getPageId()).isNull();
        assertThat(result.getBlogs().getFirst().getDisplayName()).isNull();
        assertThat(result.getBlogs().getFirst().getDisplayAvatarUrl()).isNull();
    }

    @Test
    void search_shortOrMissingQuery_returnsEmptyWithoutRepositories_TC003() {
        ExploreSearchResponseDTO missingQuery = exploreSearchService.search(null, 8);
        ExploreSearchResponseDTO shortQuery = exploreSearchService.search(" a ", 8);

        assertThat(missingQuery.getUsers()).isEmpty();
        assertThat(missingQuery.getCafePages()).isEmpty();
        assertThat(missingQuery.getBlogs()).isEmpty();
        assertThat(shortQuery.getUsers()).isEmpty();
        assertThat(shortQuery.getCafePages()).isEmpty();
        assertThat(shortQuery.getBlogs()).isEmpty();
        verifyNoInteractions(userRepository, cafePageRepository, blogRepository);
    }

    private Region region(String city) {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        region.setCity(city);
        return region;
    }

    private User user(Region region) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("cafe.user");
        user.setUserFullName("Cafe User");
        user.setUserAvatar("https://example.com/user.png");
        user.setRegion(region);
        return user;
    }

    private CafePage cafePage(Region region) {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story");
        cafePage.setAvatarUrl("https://example.com/cafe.png");
        cafePage.setAddress("Ninh Kieu");
        cafePage.setRegion(region);
        return cafePage;
    }

    private Blog blog(User author, CafePage page) {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("A calm cafe story");
        blog.setCreatedAt(LocalDateTime.of(2026, 8, 16, 10, 30));
        blog.setAuthor(author);
        blog.setPage(page);
        return blog;
    }
}
