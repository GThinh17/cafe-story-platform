package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.AdminUserResponseDTO;
import com.cafestory.entity.User;
import com.cafestory.entity.UserRoleAssignment;
import com.cafestory.entity.enums.UserRole;
import org.mapstruct.Mapper;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    default AdminUserResponseDTO toAdminUserResponseDTO(
            User user,
            Collection<UserRoleAssignment> roleAssignments) {
        AdminUserResponseDTO response = new AdminUserResponseDTO();
        response.setUserId(user.getUserId());
        response.setUserName(user.getUserName());
        response.setUserFullName(user.getUserFullName());
        response.setUserEmail(user.getUserEmail());
        response.setUserPhone(user.getUserPhone());
        response.setUserAvatar(user.getUserAvatar());
        response.setUserLike(user.getUserLike());
        response.setUserFollower(user.getUserFollower());
        response.setAccountStatus(user.getAccountStatus());
        if (user.getRegion() != null) {
            response.setRegionId(user.getRegion().getRegionId());
        }
        response.setRoles(roleAssignments.stream()
                .map(assignment -> UserRole.valueOf(assignment.getRole().getName()))
                .toList());
        return response;
    }
}
