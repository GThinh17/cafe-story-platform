package com.cafestory.repository;

import com.cafestory.entity.Conversation;
import com.cafestory.entity.enums.PageMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            select c from Conversation c
            where c.type = com.cafestory.entity.enums.ConversationType.DIRECT
            and exists (
                select m1.id from ChatMember m1
                where m1.conversation = c and m1.user.userId = :firstUserId
            )
            and exists (
                select m2.id from ChatMember m2
                where m2.conversation = c and m2.user.userId = :secondUserId
            )
            and (
                select count(m3.id) from ChatMember m3
                where m3.conversation = c
            ) = 2
            """)
    Optional<Conversation> findDirectConversation(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId);

    @Query("""
            select c from Conversation c
            left join fetch c.cafePage page
            where c.type = com.cafestory.entity.enums.ConversationType.CAFE_PAGE
            and page.id = :cafePageId
            and exists (
                select m.id from ChatMember m
                where m.conversation = c and m.user.userId = :userId
            )
            """)
    Optional<Conversation> findCafePageConversation(
            @Param("userId") UUID userId,
            @Param("cafePageId") UUID cafePageId);

    @Query("""
            select c from Conversation c
            left join fetch c.cafePage
            where exists (
                select m.id from ChatMember m
                where m.conversation = c and m.user.userId = :userId
            )
            or (
                c.type = com.cafestory.entity.enums.ConversationType.CAFE_PAGE
                and c.cafePage.owner.userId = :userId
            )
            or (
                c.type = com.cafestory.entity.enums.ConversationType.CAFE_PAGE
                and exists (
                    select pm.id from PageMember pm
                    where pm.cafePage = c.cafePage
                    and pm.user.userId = :userId
                    and pm.status = :status
                    and pm.roleName in :managerRoles
                )
            )
            order by coalesce(c.updatedAt, c.createdAt) desc, c.createdAt desc
            """)
    List<Conversation> findUserConversationsOrderByLatestActivity(
            @Param("userId") UUID userId,
            @Param("status") PageMemberStatus status,
            @Param("managerRoles") List<String> managerRoles);

    @Query("""
            select c from Conversation c
            left join fetch c.cafePage
            where c.type = com.cafestory.entity.enums.ConversationType.CAFE_PAGE
            and c.cafePage.id = :cafePageId
            order by coalesce(c.updatedAt, c.createdAt) desc, c.createdAt desc
            """)
    List<Conversation> findCafePageConversationsOrderByLatestActivity(
            @Param("cafePageId") UUID cafePageId);
}
