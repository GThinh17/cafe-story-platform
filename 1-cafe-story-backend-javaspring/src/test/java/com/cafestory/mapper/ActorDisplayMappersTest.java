package com.cafestory.mapper;

import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử các phương thức {@code default} phân giải danh tính người thực hiện
 * trong {@link BlogInteractionMapper} và {@link CommentMapper}.
 *
 * <p>Quy tắc chung cho cả hai: hành động dưới danh nghĩa trang quán thì lấy tên
 * và ảnh của trang, còn lại lấy họ tên người dùng — thiếu họ tên mới rơi về tên
 * đăng nhập. Đây là phần viết tay, không phải mã do MapStruct sinh.
 */
class ActorDisplayMappersTest {

    private final BlogInteractionMapper blogInteractionMapper = new BlogInteractionMapper() {
        @Override
        public com.cafestory.dto.responseDTO.BlogLikeResponseDTO toBlogLikeResponseDTO(
                com.cafestory.entity.BlogLike blogLike) {
            return null;
        }

        @Override
        public com.cafestory.dto.responseDTO.BlogSaveResponseDTO toBlogSaveResponseDTO(
                com.cafestory.entity.BlogSave blogSave) {
            return null;
        }

        @Override
        public com.cafestory.dto.responseDTO.BlogRatingResponseDTO toBlogRatingResponseDTO(
                com.cafestory.entity.BlogRating blogRating) {
            return null;
        }

        @Override
        public com.cafestory.dto.responseDTO.BlogShareResponseDTO toBlogShareResponseDTO(
                com.cafestory.entity.BlogShare blogShare) {
            return null;
        }
    };

    private final CommentMapper commentMapper = new CommentMapper() {
        @Override
        public com.cafestory.dto.responseDTO.CommentResponseDTO toCommentResponseDTO(
                com.cafestory.entity.Comment comment) {
            return null;
        }

        @Override
        public com.cafestory.entity.Comment toComment(
                com.cafestory.dto.requestDTO.CommentCreateDTO commentCreateDTO) {
            return null;
        }
    };

    @Test
    void resolveActorDisplayName_success_cafePageContextUsesPageName_TC001() {
        CafePage cafePage = cafePage();

        assertThat(blogInteractionMapper.resolveActorDisplayName(
                ActorContextType.CAFE_PAGE, cafePage, user("an", "Nguyen Van An")))
                .isEqualTo("Cafe Story Ninh Kieu");
        assertThat(commentMapper.resolveActorDisplayName(
                ActorContextType.CAFE_PAGE, cafePage, user("an", "Nguyen Van An")))
                .isEqualTo("Cafe Story Ninh Kieu");
    }

    @Test
    void resolveActorDisplayName_success_cafePageContextWithoutPageFallsBackToUser_TC002() {
        assertThat(blogInteractionMapper.resolveActorDisplayName(
                ActorContextType.CAFE_PAGE, null, user("an", "Nguyen Van An")))
                .isEqualTo("Nguyen Van An");
        assertThat(commentMapper.resolveActorDisplayName(
                ActorContextType.CAFE_PAGE, null, user("an", "Nguyen Van An")))
                .isEqualTo("Nguyen Van An");
    }

    @Test
    void resolveActorDisplayName_success_blankFullNameFallsBackToUserName_TC003() {
        assertThat(blogInteractionMapper.resolveActorDisplayName(
                ActorContextType.USER, cafePage(), user("an", "   ")))
                .isEqualTo("an");
        assertThat(commentMapper.resolveActorDisplayName(
                ActorContextType.USER, null, user("an", null)))
                .isEqualTo("an");
    }

    @Test
    void resolveActorDisplayName_success_nullUserGivesNull_TC004() {
        assertThat(blogInteractionMapper.resolveActorDisplayName(ActorContextType.USER, null, null)).isNull();
        assertThat(commentMapper.resolveActorDisplayName(null, null, null)).isNull();
    }

    @Test
    void resolveActorAvatarUrl_success_cafePageContextUsesPageAvatar_TC005() {
        CafePage cafePage = cafePage();

        assertThat(blogInteractionMapper.resolveActorAvatarUrl(
                ActorContextType.CAFE_PAGE, cafePage, user("an", "Nguyen Van An")))
                .isEqualTo("https://cdn.example.com/page.png");
        assertThat(commentMapper.resolveActorAvatarUrl(
                ActorContextType.CAFE_PAGE, cafePage, user("an", "Nguyen Van An")))
                .isEqualTo("https://cdn.example.com/page.png");
    }

    @Test
    void resolveActorAvatarUrl_success_userContextUsesUserAvatar_TC006() {
        assertThat(blogInteractionMapper.resolveActorAvatarUrl(
                ActorContextType.USER, cafePage(), user("an", "Nguyen Van An")))
                .isEqualTo("https://cdn.example.com/an.png");
        assertThat(commentMapper.resolveActorAvatarUrl(
                ActorContextType.CAFE_PAGE, null, user("an", "Nguyen Van An")))
                .isEqualTo("https://cdn.example.com/an.png");
    }

    @Test
    void resolveActorAvatarUrl_success_nullUserGivesNull_TC007() {
        assertThat(blogInteractionMapper.resolveActorAvatarUrl(ActorContextType.USER, null, null)).isNull();
        assertThat(commentMapper.resolveActorAvatarUrl(ActorContextType.USER, null, null)).isNull();
    }

    private User user(String userName, String fullName) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(userName);
        user.setUserFullName(fullName);
        user.setUserAvatar("https://cdn.example.com/an.png");
        return user;
    }

    private CafePage cafePage() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Story Ninh Kieu");
        cafePage.setAvatarUrl("https://cdn.example.com/page.png");
        return cafePage;
    }
}
