package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.responseDTO.BlogDisplayAuthorType;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    @Mapping(source = "author.userId", target = "authorUserId")
    @Mapping(source = "author.userName", target = "authorUserName")
    @Mapping(source = "author.userFullName", target = "authorUserFullName")
    @Mapping(source = "author.userAvatar", target = "authorUserAvatar")
    @Mapping(source = "page.id", target = "pageId")
    @Mapping(source = "page.name", target = "pageName")
    @Mapping(source = "page.avatarUrl", target = "pageAvatarUrl")
    @Mapping(target = "isLike", ignore = true)
    @Mapping(target = "isSave", ignore = true)
    @Mapping(target = "isRating", ignore = true)
    @Mapping(target = "myRating", ignore = true)
    @Mapping(target = "ratingScore", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "saveCount", ignore = true)
    @Mapping(target = "displayAuthorType", expression = "java(resolveDisplayAuthorType(blog))")
    @Mapping(target = "displayName", expression = "java(resolveDisplayName(blog))")
    @Mapping(target = "displayAvatarUrl", expression = "java(resolveDisplayAvatarUrl(blog))")
    BlogResponseDTO toBlogResponseDTO(Blog blog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "pageId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "allowComment", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "shareCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Blog toBlog(BlogCreateDTO blogCreateDTO);

    default BlogDisplayAuthorType resolveDisplayAuthorType(Blog blog) {
        return blog != null && blog.getPage() != null
                ? BlogDisplayAuthorType.CAFE_PAGE
                : BlogDisplayAuthorType.USER;
    }

    default String resolveDisplayName(Blog blog) {
        if (blog == null) {
            return null;
        }

        CafePage page = blog.getPage();
        if (page != null) {
            return page.getName();
        }

        User author = blog.getAuthor();
        if (author == null) {
            return null;
        }

        return firstNonBlank(author.getUserFullName(), author.getUserName());
    }

    default String resolveDisplayAvatarUrl(Blog blog) {
        if (blog == null) {
            return null;
        }

        CafePage page = blog.getPage();
        if (page != null) {
            return page.getAvatarUrl();
        }

        User author = blog.getAuthor();
        return author == null ? null : author.getUserAvatar();
    }

    default String firstNonBlank(String first, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        return fallback;
    }
}
