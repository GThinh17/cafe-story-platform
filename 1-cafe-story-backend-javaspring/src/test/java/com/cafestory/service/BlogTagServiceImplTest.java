package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogTaggedUser;
import com.cafestory.entity.CafePage;
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
import com.cafestory.service.serviceImplement.BlogTagServiceImpl;
import com.cafestory.service.serviceInterface.NotificationService;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogTagServiceImplTest {

    @Mock
    private BlogTaggedUserRepository blogTaggedUserRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private PageFollowRepository pageFollowRepository;

    @Mock
    private PageMemberRepository pageMemberRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private BlogTaggedUserMapper blogTaggedUserMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BlogTagServiceImpl blogTagService;

    @Test
    void syncBlogTags_success_allowsFollowingUserAndNotifies_TC001() {
        UUID actorUserId = UUID.randomUUID();
        UUID taggedUserId = UUID.randomUUID();
        Blog blog = blog(PostStatus.PUBLISHED);
        User actor = user(actorUserId, "actor");
        User taggedUser = user(taggedUserId, "tagged");

        when(userValidator.validateUserExists(actorUserId)).thenReturn(actor);
        when(userValidator.validateUserExists(taggedUserId)).thenReturn(taggedUser);
        when(blogTaggedUserRepository.findByBlogId(blog.getId())).thenReturn(List.of());
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(actorUserId, taggedUserId)).thenReturn(true);
        when(blogTaggedUserRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        blogTagService.syncBlogTags(blog, actorUserId, List.of(taggedUserId));

        verify(blogTaggedUserRepository).saveAll(any());
        verify(notificationService).createTagNotification(taggedUserId, actorUserId, blog.getId());
    }

    @Test
    void syncBlogTags_fail_ineligibleUser_TC002() {
        UUID actorUserId = UUID.randomUUID();
        UUID taggedUserId = UUID.randomUUID();
        Blog blog = blog(PostStatus.PUBLISHED);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(user(actorUserId, "actor"));
        when(userValidator.validateUserExists(taggedUserId)).thenReturn(user(taggedUserId, "tagged"));
        when(blogTaggedUserRepository.findByBlogId(blog.getId())).thenReturn(List.of());
        when(userFollowRepository.existsByFollowerUserIdAndFollowingUserId(actorUserId, taggedUserId)).thenReturn(false);
        when(pageFollowRepository.existsTaggableUserFromFollowedPages(actorUserId, taggedUserId, PageMemberStatus.ACTIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> blogTagService.syncBlogTags(blog, actorUserId, List.of(taggedUserId)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(blogTaggedUserRepository, never()).saveAll(any());
        verify(notificationService, never()).createTagNotification(any(), any(), any());
    }

    @Test
    void getTagSuggestions_success_includesFollowingAndFollowedPageUsers_TC003() {
        UUID actorUserId = UUID.randomUUID();
        User actor = user(actorUserId, "actor");
        User following = user(UUID.randomUUID(), "following-user");
        User pageOwner = user(UUID.randomUUID(), "page-owner");
        User pageMember = user(UUID.randomUUID(), "page-member");
        CafePage page = cafePage(pageOwner);

        UserFollow userFollow = new UserFollow();
        userFollow.setFollowing(following);
        PageFollow pageFollow = new PageFollow();
        pageFollow.setCafePage(page);
        PageMember member = new PageMember();
        member.setUser(pageMember);

        when(userValidator.validateUserExists(actorUserId)).thenReturn(actor);
        when(userFollowRepository.findByFollowerUserId(actorUserId)).thenReturn(List.of(userFollow));
        when(pageFollowRepository.findByUserUserId(actorUserId)).thenReturn(List.of(pageFollow));
        when(pageMemberRepository.findByCafePageIdAndStatus(page.getId(), PageMemberStatus.ACTIVE)).thenReturn(List.of(member));
        when(blogTaggedUserMapper.toBlogTaggedUserResponseDTO(any(User.class)))
                .thenAnswer(invocation -> taggedUserResponse(invocation.getArgument(0)));

        List<BlogTaggedUserResponseDTO> result = blogTagService.getTagSuggestions(actorUserId, "user");

        assertThat(result)
                .extracting(BlogTaggedUserResponseDTO::getUserName)
                .containsExactly("following-user");
    }

    private Blog blog(PostStatus status) {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(status);
        return blog;
    }

    private User user(UUID userId, String userName) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setUserEmail(userId + "@example.com");
        user.setUserPassword("secret");
        user.setAccountStatus(true);
        return user;
    }

    private CafePage cafePage(User owner) {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setOwner(owner);
        cafePage.setName("Cafe Story");
        cafePage.setAddress("Da Nang");
        return cafePage;
    }

    private BlogTaggedUserResponseDTO taggedUserResponse(User user) {
        BlogTaggedUserResponseDTO response = new BlogTaggedUserResponseDTO();
        response.setId(user.getUserId());
        response.setUserName(user.getUserName());
        return response;
    }
}
