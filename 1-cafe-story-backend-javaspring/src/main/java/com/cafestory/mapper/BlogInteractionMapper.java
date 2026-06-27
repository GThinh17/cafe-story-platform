package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;
import com.cafestory.dto.responseDTO.BlogSaveResponseDTO;
import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.BlogRating;
import com.cafestory.entity.BlogSave;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogInteractionMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "actorCafePage.id", target = "actorCafePageId")
    @Mapping(target = "actorDisplayName", expression = "java(resolveActorDisplayName(blogLike.getActorContextType(), blogLike.getActorCafePage(), blogLike.getUser()))")
    @Mapping(target = "actorAvatarUrl", expression = "java(resolveActorAvatarUrl(blogLike.getActorContextType(), blogLike.getActorCafePage(), blogLike.getUser()))")
    BlogLikeResponseDTO toBlogLikeResponseDTO(BlogLike blogLike);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(target = "saved", ignore = true)
    @Mapping(target = "saveCount", ignore = true)
    BlogSaveResponseDTO toBlogSaveResponseDTO(BlogSave blogSave);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(target = "ratingAverage", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    BlogRatingResponseDTO toBlogRatingResponseDTO(BlogRating blogRating);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "actorCafePage.id", target = "actorCafePageId")
    @Mapping(target = "actorDisplayName", expression = "java(resolveActorDisplayName(blogShare.getActorContextType(), blogShare.getActorCafePage(), blogShare.getUser()))")
    @Mapping(target = "actorAvatarUrl", expression = "java(resolveActorAvatarUrl(blogShare.getActorContextType(), blogShare.getActorCafePage(), blogShare.getUser()))")
    BlogShareResponseDTO toBlogShareResponseDTO(BlogShare blogShare);

    default String resolveActorDisplayName(ActorContextType actorContextType, CafePage actorCafePage, User user) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getName();
        }
        if (user == null) {
            return null;
        }
        if (user.getUserFullName() != null && !user.getUserFullName().isBlank()) {
            return user.getUserFullName();
        }
        return user.getUserName();
    }

    default String resolveActorAvatarUrl(ActorContextType actorContextType, CafePage actorCafePage, User user) {
        if (actorContextType == ActorContextType.CAFE_PAGE && actorCafePage != null) {
            return actorCafePage.getAvatarUrl();
        }
        return user == null ? null : user.getUserAvatar();
    }
}
