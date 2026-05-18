package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogShare;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogInteractionMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    BlogLikeResponseDTO toBlogLikeResponseDTO(BlogLike blogLike);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    BlogShareResponseDTO toBlogShareResponseDTO(BlogShare blogShare);
}
