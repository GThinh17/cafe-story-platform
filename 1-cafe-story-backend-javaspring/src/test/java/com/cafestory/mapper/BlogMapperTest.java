package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlogMapperTest {

    private final BlogMapper blogMapper = Mappers.getMapper(BlogMapper.class);

    @Test
    void toBlogResponseDTO_userBlog_usesUserDisplayFields() {
        User author = user();
        Blog blog = blog(author);
        blog.setPage(null);

        BlogResponseDTO result = blogMapper.toBlogResponseDTO(blog);

        assertThat(result.getAuthorUserId()).isEqualTo(author.getUserId());
        assertThat(result.getAuthorUserName()).isEqualTo("reader");
        assertThat(result.getAuthorUserFullName()).isEqualTo("Reader Name");
        assertThat(result.getAuthorUserAvatar()).isEqualTo("/images/users/reader.png");
        assertThat(result.getPageId()).isNull();
        assertThat(result.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.USER);
        assertThat(result.getDisplayName()).isEqualTo("reader");
        assertThat(result.getDisplayAvatarUrl()).isEqualTo("/images/users/reader.png");
    }

    @Test
    void toBlogResponseDTO_cafePageBlog_usesCafePageDisplayFieldsAndKeepsAuthor() {
        User author = user();
        CafePage page = cafePage();
        Blog blog = blog(author);
        blog.setPage(page);

        BlogResponseDTO result = blogMapper.toBlogResponseDTO(blog);

        assertThat(result.getAuthorUserId()).isEqualTo(author.getUserId());
        assertThat(result.getAuthorUserName()).isEqualTo("reader");
        assertThat(result.getPageId()).isEqualTo(page.getId());
        assertThat(result.getPageName()).isEqualTo("Cafe Story Roastery");
        assertThat(result.getPageAvatarUrl()).isEqualTo("/images/cafes/roastery.png");
        assertThat(result.getDisplayAuthorType()).isEqualTo(BlogDisplayAuthorType.CAFE_PAGE);
        assertThat(result.getDisplayName()).isEqualTo("Cafe Story Roastery");
        assertThat(result.getDisplayAvatarUrl()).isEqualTo("/images/cafes/roastery.png");
    }

    private Blog blog(User author) {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(author);
        blog.setContent("Cafe Story blog");
        blog.setImageUrls(List.of("/images/blog.png"));
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setIsPinned(false);
        blog.setAllowComment(true);
        blog.setLikeCount(12);
        blog.setCommentCount(3);
        blog.setShareCount(4);
        return blog;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("reader");
        user.setUserFullName("Reader Name");
        user.setUserAvatar("/images/users/reader.png");
        return user;
    }

    private CafePage cafePage() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story Roastery");
        cafePage.setAvatarUrl("/images/cafes/roastery.png");
        return cafePage;
    }
}
