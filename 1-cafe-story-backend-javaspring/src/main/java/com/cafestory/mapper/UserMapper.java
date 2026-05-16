package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userLike", ignore = true)
    @Mapping(target = "userFollower", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    User toUser(UserCreateDTO userCreateDTO);
}