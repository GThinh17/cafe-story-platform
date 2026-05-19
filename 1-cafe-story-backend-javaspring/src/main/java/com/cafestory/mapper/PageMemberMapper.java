package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.PageMemberResponseDTO;
import com.cafestory.entity.PageMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PageMemberMapper {

    @Mapping(source = "cafePage.id", target = "cafePageId")
    @Mapping(source = "user.userId", target = "userId")
    PageMemberResponseDTO toPageMemberResponseDTO(PageMember pageMember);
}
