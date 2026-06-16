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

    @Query("""
            select lower(u.userName)
            from User u
            where lower(u.userName) in :userNames
            """)
    List<String> findExistingUserNamesLowercase(@Param("userNames") List<String> userNames);

    List<User> findByAccountStatusTrue();

    @Query("""
            select distinct u
            from User u
            left join fetch u.region r
            where u.accountStatus = true
            and u.userId <> :currentUserId
            and not exists (
                select uf
                from UserFollow uf
                where uf.follower.userId = :currentUserId
                and uf.following.userId = u.userId
            )
            """)
    List<User> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            Pageable pageable);

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
