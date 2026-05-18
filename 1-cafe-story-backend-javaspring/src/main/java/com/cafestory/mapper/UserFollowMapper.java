package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.UserFollowResponseDTO;
import com.cafestory.entity.UserFollow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserFollowMapper {

    @Mapping(source = "follower.userId", target = "followerUserId")
    @Mapping(source = "following.userId", target = "followingUserId")
    UserFollowResponseDTO toUserFollowResponseDTO(UserFollow userFollow);
}
