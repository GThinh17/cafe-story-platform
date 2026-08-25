package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.userName", target = "authorUserName")
    @Mapping(source = "user.userAvatar", target = "authorUserAvatar")
    @Mapping(source = "actorCafePage.id", target = "actorCafePageId")
    @Mapping(target = "actorDisplayName", expression = "java(resolveActorDisplayName(comment.getActorContextType(), comment.getActorCafePage(), comment.getUser()))")
    @Mapping(target = "actorAvatarUrl", expression = "java(resolveActorAvatarUrl(comment.getActorContextType(), comment.getActorCafePage(), comment.getUser()))")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    CommentResponseDTO toCommentResponseDTO(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "actorCafePage", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toComment(CommentCreateDTO commentCreateDTO);

    default String resolveActorDisplayName(ActorContextType actorContextType, CafePage actorCafePage, User user) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getName();
        }
        if (user == null) {
            return null;
        }
        if (user.getUserFullName() != null && !user.getUserFullName().isBlank()) {
            return user.getUserFullName();
        }
        return user.getUserName();
    }

    default String resolveActorAvatarUrl(ActorContextType actorContextType, CafePage actorCafePage, User user) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getAvatarUrl();
        }
        return user == null ? null : user.getUserAvatar();
    }
}
