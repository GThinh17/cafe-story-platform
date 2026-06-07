package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.UserCreateDTO;
import com.cafestory.dto.responseDTO.UserResponseDTO;
import com.cafestory.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "region.regionId", target = "regionId")
    @Mapping(source = "region.city", target = "regionCity")
    @Mapping(source = "region.province", target = "regionProvince")
    @Mapping(source = "region.ward", target = "regionWard")
    @Mapping(source = "region.area", target = "regionArea")
    @Mapping(source = "region.street", target = "regionStreet")
    @Mapping(target = "isFollowing", ignore = true)
    @Mapping(target = "followingCount", ignore = true)
    UserResponseDTO toUserResponseDTO(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userLike", ignore = true)
    @Mapping(target = "userFollower", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "region", ignore = true)
    User toUser(UserCreateDTO userCreateDTO);
}
