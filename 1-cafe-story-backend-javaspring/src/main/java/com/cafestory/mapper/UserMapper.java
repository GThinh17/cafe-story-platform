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
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "district", ignore = true)
    @Mapping(target = "ward", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    User toUser(UserCreateDTO userCreateDTO);
}
