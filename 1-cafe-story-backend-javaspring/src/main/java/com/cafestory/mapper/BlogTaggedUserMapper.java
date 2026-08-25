package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.BlogTaggedUser;
import com.cafestory.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogTaggedUserMapper {

    @Mapping(source = "taggedUser.userId", target = "id")
    @Mapping(source = "taggedUser.userName", target = "userName")
    BlogTaggedUserResponseDTO toBlogTaggedUserResponseDTO(BlogTaggedUser blogTaggedUser);

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "userName", target = "userName")
    BlogTaggedUserResponseDTO toBlogTaggedUserResponseDTO(User user);
}
