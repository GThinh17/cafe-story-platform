package com.cafestory.mapper;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.CafePage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CafePageMapper {

    @Mapping(source = "owner.userId", target = "ownerUserId")
    CafePageResponseDTO toCafePageResponseDTO(CafePage cafePage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "followerCount", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CafePage toCafePage(CafePageCreateDTO cafePageCreateDTO);
}
