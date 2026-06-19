package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.CommentCreateDTO;
import com.cafestory.dto.responseDTO.CommentResponseDTO;
import com.cafestory.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.userName", target = "authorUserName")
    @Mapping(source = "user.userAvatar", target = "authorUserAvatar")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    CommentResponseDTO toCommentResponseDTO(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toComment(CommentCreateDTO commentCreateDTO);
}
