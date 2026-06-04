package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.CafePage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CafePageMapper {

    @Mapping(source = "owner.userId", target = "ownerUserId")
    @Mapping(source = "region.regionId", target = "regionId")
    @Mapping(source = "region.city", target = "regionCity")
    @Mapping(source = "region.province", target = "regionProvince")
    @Mapping(source = "region.ward", target = "regionWard")
    @Mapping(source = "region.area", target = "regionArea")
    @Mapping(source = "region.street", target = "regionStreet")
    @Mapping(target = "isFollowing", ignore = true)
    @Mapping(target = "isLiked", ignore = true)
    @Mapping(target = "isRating", ignore = true)
    @Mapping(target = "myRating", ignore = true)
    @Mapping(target = "ratingScore", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    CafePageResponseDTO toCafePageResponseDTO(CafePage cafePage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "followerCount", ignore = true)
    @Mapping(target = "maxMembers", ignore = true)
    @Mapping(target = "pageActive", ignore = true)
    @Mapping(target = "pageExpiresAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CafePage toCafePage(CafePageCreateDTO cafePageCreateDTO);
}
