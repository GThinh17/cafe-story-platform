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
            select u
            from User u
            left join fetch u.region r
            where u.accountStatus = true
            and u.userId <> :currentUserId
            order by
                case
                    when r.regionId = :currentRegionId then 0
                    when lower(r.city) = :currentCity then 1
                    else 2
                end,
                coalesce(u.userFollower, 0) desc,
                coalesce(u.userLike, 0) desc,
                u.userName asc
            """)
    List<User> findRecommendationCandidates(
            @Param("currentUserId") UUID currentUserId,
            @Param("currentRegionId") UUID currentRegionId,
            @Param("currentCity") String currentCity,
            Pageable pageable);

    long countByAccountStatus(Boolean accountStatus);

    @Query("""
            select distinct u
            from User u
            left join UserRoleAssignment assignment on assignment.user = u
            where (cast(:search as string) is null
                or lower(u.userName) like lower(concat('%', cast(:search as string), '%'))
                or lower(u.userEmail) like lower(concat('%', cast(:search as string), '%'))
                or lower(coalesce(u.userFullName, '')) like lower(concat('%', cast(:search as string), '%')))
            and (cast(:accountStatus as boolean) is null or u.accountStatus = :accountStatus)
            and (cast(:roleName as string) is null or assignment.role.name = :roleName)
            """)
    Page<User> findAdminUsers(
            @Param("search") String search,
            @Param("accountStatus") Boolean accountStatus,
            @Param("roleName") String roleName,
            Pageable pageable);

    @Query("""
            select p.provinceCode, count(u)
            from User u
            join u.region r
            join r.provinceRef p
            group by p.provinceCode
            """)
    List<Object[]> countUsersByProvince();
}
