package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.PageFollowResponseDTO;
import com.cafestory.dto.responseDTO.PageLikeResponseDTO;
import com.cafestory.entity.PageFollow;
import com.cafestory.entity.PageLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CafePageInteractionMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "cafePage.id", target = "cafePageId")
    PageLikeResponseDTO toPageLikeResponseDTO(PageLike pageLike);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "cafePage.id", target = "cafePageId")
    PageFollowResponseDTO toPageFollowResponseDTO(PageFollow pageFollow);
}
