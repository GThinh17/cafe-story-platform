package com.cafestory.repository;

import com.cafestory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUserEmail(String userEmail);

    boolean existsByUserName(String userName);

    Optional<User> findByUserName(String userName);

    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByUserEmailOrUserName(String userEmail, String userName);

    List<User> findByAccountStatusTrue();

    long countByAccountStatus(Boolean accountStatus);

    @Query("""
            select distinct u
            from User u
            left join UserRoleAssignment assignment on assignment.user = u
            where (:search is null
                or lower(u.userName) like lower(concat('%', :search, '%'))
                or lower(u.userEmail) like lower(concat('%', :search, '%'))
                or lower(coalesce(u.userFullName, '')) like lower(concat('%', :search, '%')))
            and (:accountStatus is null or u.accountStatus = :accountStatus)
            and (:roleName is null or assignment.role.name = :roleName)
            """)
    Page<User> findAdminUsers(
            @Param("search") String search,
            @Param("accountStatus") Boolean accountStatus,
            @Param("roleName") String roleName,
            Pageable pageable);
}
