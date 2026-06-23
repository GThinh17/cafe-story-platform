package com.cafestory.service;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.requestDTO.UserUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.RegionCity;
import com.cafestory.entity.RegionProvince;
import com.cafestory.entity.RegionWard;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.mapper.UserMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.repository.BlogTaggedUserRepository;
import com.cafestory.repository.PageFollowRepository;
import com.cafestory.repository.RegionCityRepository;
import com.cafestory.repository.RegionProvinceRepository;
import com.cafestory.repository.RegionRepository;
import com.cafestory.repository.RegionWardRepository;
import com.cafestory.repository.ReportReasonRepository;
import com.cafestory.repository.UserFollowRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.BlogServiceImpl;
import com.cafestory.service.serviceImplement.RegionServiceImpl;
import com.cafestory.service.serviceImplement.ReportReasonServiceImpl;
import com.cafestory.service.serviceImplement.UserProfileCacheService;
import com.cafestory.service.serviceImplement.UserServiceImpl;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.service.serviceInterface.AiBlogModerationService;
import com.cafestory.service.serviceInterface.BlogTagService;
import com.cafestory.service.serviceInterface.RegionService;
import com.cafestory.service.serviceInterface.ReportReasonService;
import com.cafestory.service.serviceInterface.UserService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = CacheableServiceTest.TestCacheConfig.class)
class CacheableServiceTest {

    @jakarta.annotation.Resource
    private RegionService regionService;

    @jakarta.annotation.Resource
    private ReportReasonService reportReasonService;

    @jakarta.annotation.Resource
    private BlogService blogService;

    @jakarta.annotation.Resource
    private UserService userService;

    @jakarta.annotation.Resource
    private RegionProvinceRepository provinceRepository;

    @jakarta.annotation.Resource
    private RegionCityRepository cityRepository;

    @jakarta.annotation.Resource
    private RegionWardRepository wardRepository;

    @jakarta.annotation.Resource
    private ReportReasonRepository reportReasonRepository;

    @jakarta.annotation.Resource
    private BlogValidator blogValidator;

    @jakarta.annotation.Resource
    private BlogMapper blogMapper;

    @jakarta.annotation.Resource
    private BlogRepository blogRepository;

    @jakarta.annotation.Resource
    private BlogLikeRepository blogLikeRepository;

    @jakarta.annotation.Resource
    private BlogSaveRepository blogSaveRepository;

    @jakarta.annotation.Resource
    private BlogRatingRepository blogRatingRepository;

    @jakarta.annotation.Resource
    private BlogTaggedUserRepository blogTaggedUserRepository;

    @jakarta.annotation.Resource
    private BlogTagService blogTagService;

    @jakarta.annotation.Resource
    private UserValidator userValidator;

    @jakarta.annotation.Resource
    private UserMapper userMapper;

    @jakarta.annotation.Resource
    private UserRepository userRepository;

    @jakarta.annotation.Resource
    private UserFollowRepository userFollowRepository;

    @jakarta.annotation.Resource
    private PageFollowRepository pageFollowRepository;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(
                provinceRepository,
                cityRepository,
                wardRepository,
                reportReasonRepository,
                blogValidator,
                blogMapper,
                blogRepository,
                blogLikeRepository,
                blogSaveRepository,
                blogRatingRepository,
                blogTaggedUserRepository,
                blogTagService,
                userValidator,
                userMapper,
                userRepository,
                userFollowRepository,
                pageFollowRepository);
        clearCache(CacheConfig.REGION_PROVINCES_CACHE);
        clearCache(CacheConfig.REGION_CITIES_CACHE);
        clearCache(CacheConfig.REGION_WARDS_CACHE);
        clearCache(CacheConfig.REPORT_REASONS_CACHE);
        clearCache(CacheConfig.ORGANIC_FEED_CACHE);
        clearCache(CacheConfig.BLOG_DETAIL_CACHE);
        clearCache(CacheConfig.USER_PROFILE_BY_ID_CACHE);
        clearCache(CacheConfig.USER_PROFILE_BY_USERNAME_CACHE);
        clearCache(CacheConfig.USER_PROFILE_BLOGS_CACHE);
        clearCache(CacheConfig.USER_FOLLOWING_COUNT_CACHE);
    }

    @Test
    void getProvinces_success_usesCacheForRepeatedCalls_TC001() {
        when(provinceRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(province("79", "Ho Chi Minh")));

        assertThat(regionService.getProvinces()).hasSize(1);
        assertThat(regionService.getProvinces()).hasSize(1);

        verify(provinceRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    void getCities_success_usesDifferentCacheKeysPerProvince_TC002() {
        when(cityRepository.findByProvinceProvinceCodeOrderByNameAsc("79"))
                .thenReturn(List.of(city("79-1", "79", "Ho Chi Minh")));
        when(cityRepository.findByProvinceProvinceCodeOrderByNameAsc("01"))
                .thenReturn(List.of(city("01-1", "01", "Ha Noi")));

        assertThat(regionService.getCities("79")).extracting("cityCode").containsExactly("79-1");
        assertThat(regionService.getCities("79")).extracting("cityCode").containsExactly("79-1");
        assertThat(regionService.getCities("01")).extracting("cityCode").containsExactly("01-1");

        verify(cityRepository, times(1)).findByProvinceProvinceCodeOrderByNameAsc("79");
        verify(cityRepository, times(1)).findByProvinceProvinceCodeOrderByNameAsc("01");
    }

    @Test
    void getWards_success_usesDifferentCacheKeysPerCity_TC003() {
        when(wardRepository.findByCityCityCodeOrderByNameAsc("79-1"))
                .thenReturn(List.of(ward("26734", "79", "79-1", "Ben Nghe")));
        when(wardRepository.findByCityCityCodeOrderByNameAsc("01-1"))
                .thenReturn(List.of(ward("00001", "01", "01-1", "Phuc Xa")));

        assertThat(regionService.getWards("79", "79-1")).extracting("wardCode").containsExactly("26734");
        assertThat(regionService.getWards("79", "79-1")).extracting("wardCode").containsExactly("26734");
        assertThat(regionService.getWards("01", "01-1")).extracting("wardCode").containsExactly("00001");

        verify(wardRepository, times(1)).findByCityCityCodeOrderByNameAsc("79-1");
        verify(wardRepository, times(1)).findByCityCityCodeOrderByNameAsc("01-1");
    }

    @Test
    void getActiveReportReasons_success_usesDifferentCacheKeysPerTargetType_TC004() {
        when(reportReasonRepository.findActiveReasonsForTargetType(ReportTargetType.BLOG))
                .thenReturn(List.of(reason("SPAM", ReportTargetType.BLOG)));
        when(reportReasonRepository.findActiveReasonsForTargetType(ReportTargetType.USER))
                .thenReturn(List.of(reason("FAKE_ACCOUNT", ReportTargetType.USER)));

        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.BLOG))
                .extracting("code").containsExactly("SPAM");
        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.BLOG))
                .extracting("code").containsExactly("SPAM");
        assertThat(reportReasonService.getActiveReportReasons(ReportTargetType.USER))
                .extracting("code").containsExactly("FAKE_ACCOUNT");

        verify(reportReasonRepository, times(1)).findActiveReasonsForTargetType(ReportTargetType.BLOG);
        verify(reportReasonRepository, times(1)).findActiveReasonsForTargetType(ReportTargetType.USER);
    }

    @Test
    void getBlogById_success_cachesAnonymousButNotViewerSpecific_TC005() {
        UUID blogId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        Blog blog = blog(blogId);
        BlogResponseDTO anonymousResponse = blogResponse(blogId);
        BlogResponseDTO viewerResponse = blogResponse(blogId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(anonymousResponse, viewerResponse, viewerResponse);

        assertThat(blogService.getBlogById(blogId, null)).isEqualTo(anonymousResponse);
        assertThat(blogService.getBlogById(blogId, null)).isEqualTo(anonymousResponse);
        assertThat(blogService.getBlogById(blogId, viewerUserId)).isEqualTo(viewerResponse);
        assertThat(blogService.getBlogById(blogId, viewerUserId)).isEqualTo(viewerResponse);

        verify(blogValidator, times(3)).validateBlogExists(blogId);
    }

    @Test
    void updateBlog_success_evictsBlogDetailAndOrganicFeed_TC006() {
        UUID blogId = UUID.randomUUID();
        Blog blog = blog(blogId);
        BlogUpdateDTO request = new BlogUpdateDTO();
        request.setContent("Updated content");

        cache(CacheConfig.BLOG_DETAIL_CACHE).put(blogId, blogResponse(blogId));
        cache(CacheConfig.ORGANIC_FEED_CACHE).put("first:20", "cached-feed");
        cache(CacheConfig.USER_PROFILE_BLOGS_CACHE).put(blog.getAuthor().getUserId() + ":anon", List.of(blogResponse(blogId)));
        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.toBlogResponseDTO(blog)).thenReturn(blogResponse(blogId));

        blogService.updateBlog(blogId, blog.getAuthor().getUserId(), request);

        assertThat(cache(CacheConfig.BLOG_DETAIL_CACHE).get(blogId)).isNull();
        assertThat(cache(CacheConfig.ORGANIC_FEED_CACHE).get("first:20")).isNull();
        assertThat(cache(CacheConfig.USER_PROFILE_BLOGS_CACHE).get(blog.getAuthor().getUserId() + ":anon")).isNull();
    }

    @Test
    void getUserById_success_cachesAnonymousButNotViewerSpecific_TC007() {
        UUID userId = UUID.randomUUID();
        UUID viewerUserId = UUID.randomUUID();
        User user = user(userId);
        UserResponseDTO anonymousResponse = userResponse(userId);
        UserResponseDTO viewerResponse = userResponse(userId);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(anonymousResponse, viewerResponse, viewerResponse);

        assertThat(userService.getUserById(userId, null)).isEqualTo(anonymousResponse);
        assertThat(userService.getUserById(userId, null)).isEqualTo(anonymousResponse);
        assertThat(userService.getUserById(userId, viewerUserId)).isEqualTo(viewerResponse);
        assertThat(userService.getUserById(userId, viewerUserId)).isEqualTo(viewerResponse);

        verify(userValidator, times(3)).validateUserExists(userId);
    }

    @Test
    void updateUser_success_evictsUserProfileCaches_TC008() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        UserUpdateDTO request = new UserUpdateDTO();
        request.setUserFullName("Updated User");

        cache(CacheConfig.USER_PROFILE_BY_ID_CACHE).put(userId, userResponse(userId));
        cache(CacheConfig.USER_PROFILE_BY_USERNAME_CACHE).put(user.getUserName(), userResponse(userId));
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponse(userId));

        userService.updateUser(userId, request);

        assertThat(cache(CacheConfig.USER_PROFILE_BY_ID_CACHE).get(userId)).isNull();
        assertThat(cache(CacheConfig.USER_PROFILE_BY_USERNAME_CACHE).get(user.getUserName())).isNull();
    }

    @Test
    void getUserById_success_cachesSelfProfile_TC009() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenAnswer(invocation -> userResponse(userId));

        assertThat(userService.getUserById(userId, userId).getUserId()).isEqualTo(userId);
        assertThat(userService.getUserById(userId, userId).getUserId()).isEqualTo(userId);

        verify(userValidator, times(1)).validateUserExists(userId);
    }

    @Test
    void getAllBlogsByUserId_success_cachesSelfProfileBlogsButNotOtherViewer_TC010() {
        UUID userId = UUID.randomUUID();
        UUID otherViewerId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        Blog blog = blog(blogId);
        blog.setAuthor(user(userId));

        when(userValidator.validateUserExists(userId)).thenReturn(user(userId));
        when(blogRepository.findByAuthorUserId(userId)).thenReturn(List.of(blog));
        when(blogMapper.toBlogResponseDTO(blog)).thenAnswer(invocation -> blogResponse(blogId));

        assertThat(blogService.getAllBlogsByUserId(userId, userId)).hasSize(1);
        assertThat(blogService.getAllBlogsByUserId(userId, userId)).hasSize(1);
        assertThat(blogService.getAllBlogsByUserId(userId, otherViewerId)).hasSize(1);
        assertThat(blogService.getAllBlogsByUserId(userId, otherViewerId)).hasSize(1);

        verify(blogRepository, times(2)).findByAuthorUserId(userId);
    }

    @Test
    void getUserById_success_cachesFollowingCountAcrossViewerSpecificProfiles_TC011() {
        UUID userId = UUID.randomUUID();
        UUID firstViewerId = UUID.randomUUID();
        UUID secondViewerId = UUID.randomUUID();
        User user = user(userId);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(userMapper.toUserResponseDTO(user)).thenAnswer(invocation -> userResponse(userId));
        when(userFollowRepository.countByFollowerUserId(userId)).thenReturn(5L);
        when(pageFollowRepository.countByUserUserId(userId)).thenReturn(2L);

        assertThat(userService.getUserById(userId, firstViewerId).getFollowingCount()).isEqualTo(7);
        assertThat(userService.getUserById(userId, secondViewerId).getFollowingCount()).isEqualTo(7);

        verify(userFollowRepository, times(1)).countByFollowerUserId(userId);
        verify(pageFollowRepository, times(1)).countByUserUserId(userId);
    }

    private void clearCache(String cacheName) {
        cache(cacheName).clear();
    }

    private org.springframework.cache.Cache cache(String cacheName) {
        return Objects.requireNonNull(cacheManager.getCache(cacheName));
    }

    private RegionProvince province(String provinceCode, String name) {
        RegionProvince province = new RegionProvince();
        province.setProvinceCode(provinceCode);
        province.setName(name);
        return province;
    }

    private RegionCity city(String cityCode, String provinceCode, String name) {
        RegionCity city = new RegionCity();
        city.setCityCode(cityCode);
        city.setProvince(province(provinceCode, name));
        city.setName(name);
        return city;
    }

    private RegionWard ward(String wardCode, String provinceCode, String cityCode, String name) {
        RegionWard ward = new RegionWard();
        ward.setWardCode(wardCode);
        ward.setProvince(province(provinceCode, name));
        ward.setCity(city(cityCode, provinceCode, name));
        ward.setName(name);
        return ward;
    }

    private ReportReason reason(String code, ReportTargetType targetType) {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode(code);
        reason.setLabelVi(code);
        reason.setTargetType(targetType);
        reason.setSeverity(1);
        reason.setRequiresDescription(false);
        reason.setActive(true);
        reason.setSortOrder(1);
        return reason;
    }

    private Blog blog(UUID blogId) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(user(UUID.randomUUID()));
        blog.setContent("Cafe Story post");
        return blog;
    }

    private BlogResponseDTO blogResponse(UUID blogId) {
        BlogResponseDTO response = new BlogResponseDTO();
        response.setId(blogId);
        response.setContent("Cafe Story post");
        return response;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("cafestory_user");
        user.setUserEmail("user@example.com");
        user.setUserPassword("encoded");
        user.setAccountStatus(true);
        return user;
    }

    private UserResponseDTO userResponse(UUID userId) {
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(userId);
        response.setUserName("cafestory_user");
        response.setAccountStatus(true);
        return response;
    }

    @Configuration
    @EnableCaching
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    CacheConfig.REGION_PROVINCES_CACHE,
                    CacheConfig.REGION_CITIES_CACHE,
                    CacheConfig.REGION_WARDS_CACHE,
                    CacheConfig.REPORT_REASONS_CACHE,
                    CacheConfig.ORGANIC_FEED_CACHE,
                    CacheConfig.PERSONALIZED_FEED_RANKING_CACHE,
                    CacheConfig.CAFE_PAGE_BLOGS_CACHE,
                    CacheConfig.TRENDING_BLOGS_CACHE,
                    CacheConfig.BLOG_DETAIL_CACHE,
                    CacheConfig.USER_PROFILE_BY_ID_CACHE,
                    CacheConfig.USER_PROFILE_BY_USERNAME_CACHE,
                    CacheConfig.USER_PROFILE_BLOGS_CACHE,
                    CacheConfig.USER_FOLLOWING_COUNT_CACHE);
        }

        @Bean
        RegionService regionService(
                RegionRepository regionRepository,
                RegionProvinceRepository provinceRepository,
                RegionCityRepository cityRepository,
                RegionWardRepository wardRepository) {
            return new RegionServiceImpl(regionRepository, provinceRepository, cityRepository, wardRepository);
        }

        @Bean
        ReportReasonService reportReasonService(ReportReasonRepository reportReasonRepository) {
            return new ReportReasonServiceImpl(reportReasonRepository);
        }

        @Bean
        BlogService blogService(
                BlogRepository blogRepository,
                BlogLikeRepository blogLikeRepository,
                BlogSaveRepository blogSaveRepository,
                BlogRatingRepository blogRatingRepository,
                BlogTaggedUserRepository blogTaggedUserRepository,
                RegionRepository regionRepository,
                RegionService regionService,
                AiBlogModerationService aiBlogModerationService,
                BlogMapper blogMapper,
                BlogValidator blogValidator,
                CafePageValidator cafePageValidator,
                UserValidator userValidator,
                BlogTagService blogTagService) {
            return new BlogServiceImpl(
                    blogRepository,
                    blogLikeRepository,
                    blogSaveRepository,
                    blogRatingRepository,
                    blogTaggedUserRepository,
                    regionRepository,
                    regionService,
                    aiBlogModerationService,
                    blogMapper,
                    blogValidator,
                    cafePageValidator,
                    userValidator,
                    blogTagService);
        }

        @Bean
        AiBlogModerationService aiBlogModerationService() {
            return Mockito.mock(AiBlogModerationService.class);
        }

        @Bean
        UserService userService(
                UserRepository userRepository,
                UserFollowRepository userFollowRepository,
                RegionService regionService,
                UserMapper userMapper,
                UserValidator userValidator,
                PasswordEncoder passwordEncoder,
                UserProfileCacheService userProfileCacheService) {
            return new UserServiceImpl(
                    userRepository,
                    userFollowRepository,
                    regionService,
                    userMapper,
                    userValidator,
                    passwordEncoder,
                    userProfileCacheService);
        }

        @Bean
        UserProfileCacheService userProfileCacheService(
                UserFollowRepository userFollowRepository,
                PageFollowRepository pageFollowRepository) {
            return new UserProfileCacheService(userFollowRepository, pageFollowRepository);
        }

        @Bean
        RegionRepository regionRepository() {
            return Mockito.mock(RegionRepository.class);
        }

        @Bean
        RegionProvinceRepository provinceRepository() {
            return Mockito.mock(RegionProvinceRepository.class);
        }

        @Bean
        RegionCityRepository cityRepository() {
            return Mockito.mock(RegionCityRepository.class);
        }

        @Bean
        RegionWardRepository wardRepository() {
            return Mockito.mock(RegionWardRepository.class);
        }

        @Bean
        ReportReasonRepository reportReasonRepository() {
            return Mockito.mock(ReportReasonRepository.class);
        }

        @Bean
        BlogRepository blogRepository() {
            return Mockito.mock(BlogRepository.class);
        }

        @Bean
        BlogLikeRepository blogLikeRepository() {
            return Mockito.mock(BlogLikeRepository.class);
        }

        @Bean
        BlogSaveRepository blogSaveRepository() {
            return Mockito.mock(BlogSaveRepository.class);
        }

        @Bean
        BlogRatingRepository blogRatingRepository() {
            return Mockito.mock(BlogRatingRepository.class);
        }

        @Bean
        BlogTaggedUserRepository blogTaggedUserRepository() {
            return Mockito.mock(BlogTaggedUserRepository.class);
        }

        @Bean
        BlogMapper blogMapper() {
            return Mockito.mock(BlogMapper.class);
        }

        @Bean
        BlogValidator blogValidator() {
            return Mockito.mock(BlogValidator.class);
        }

        @Bean
        CafePageValidator cafePageValidator() {
            return Mockito.mock(CafePageValidator.class);
        }

        @Bean
        BlogTagService blogTagService() {
            return Mockito.mock(BlogTagService.class);
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        UserFollowRepository userFollowRepository() {
            return Mockito.mock(UserFollowRepository.class);
        }

        @Bean
        PageFollowRepository pageFollowRepository() {
            return Mockito.mock(PageFollowRepository.class);
        }

        @Bean
        UserMapper userMapper() {
            return Mockito.mock(UserMapper.class);
        }

        @Bean
        UserValidator userValidator() {
            return Mockito.mock(UserValidator.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return Mockito.mock(PasswordEncoder.class);
        }
    }
}
