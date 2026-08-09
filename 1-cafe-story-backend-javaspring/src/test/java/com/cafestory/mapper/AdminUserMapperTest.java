package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.Region;
import com.cafestory.entity.Role;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm thử phương thức mặc định của {@link AdminUserMapper}.
 *
 * <p>MapStruct sinh lớp cài đặt cho interface này, nhưng phần logic thật nằm ở
 * phương thức {@code default} viết tay nên được kiểm trực tiếp qua một cài đặt
 * rỗng.
 */
class AdminUserMapperTest {

    private final AdminUserMapper mapper = new AdminUserMapper() {
    };

    @Test
    void toAdminUserResponseDTO_success_mapsProfileRegionAndRoles_TC001() {
        Region region = new Region();
        region.setRegionId(UUID.randomUUID());
        User user = user();
        user.setRegion(region);
        List<UserRoleAssignment> assignments = List.of(
                assignment(user, UserRole.USER),
                assignment(user, UserRole.REVIEWER));

        AdminUserResponseDTO result = mapper.toAdminUserResponseDTO(user, assignments);

        assertThat(result.getUserId()).isEqualTo(user.getUserId());
        assertThat(result.getUserName()).isEqualTo("an");
        assertThat(result.getUserFullName()).isEqualTo("Nguyen Van An");
        assertThat(result.getUserEmail()).isEqualTo("an@example.com");
        assertThat(result.getUserPhone()).isEqualTo(900000000L);
        assertThat(result.getUserAvatar()).isEqualTo("https://cdn.example.com/an.png");
        assertThat(result.getUserLike()).isEqualTo(12);
        assertThat(result.getUserFollower()).isEqualTo(34);
        assertThat(result.getAccountStatus()).isTrue();
        assertThat(result.getRegionId()).isEqualTo(region.getRegionId());
        assertThat(result.getRoles()).containsExactly(UserRole.USER, UserRole.REVIEWER);
    }

    @Test
    void toAdminUserResponseDTO_success_userWithoutRegionOrRoles_TC002() {
        AdminUserResponseDTO result = mapper.toAdminUserResponseDTO(user(), List.of());

        assertThat(result.getRegionId()).isNull();
        assertThat(result.getRoles()).isEmpty();
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("an");
        user.setUserFullName("Nguyen Van An");
        user.setUserEmail("an@example.com");
        user.setUserPhone(900000000L);
        user.setUserAvatar("https://cdn.example.com/an.png");
        user.setUserLike(12);
        user.setUserFollower(34);
        user.setAccountStatus(true);
        return user;
    }

    private UserRoleAssignment assignment(User user, UserRole userRole) {
        Role role = new Role();
        role.setId(userRole.ordinal() + 1);
        role.setName(userRole.name());
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(role);
        return assignment;
    }
}
