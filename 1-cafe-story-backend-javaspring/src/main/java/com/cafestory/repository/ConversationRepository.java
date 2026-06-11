package com.cafestory.repository;

import com.cafestory.entity.Conversation;
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
            where exists (
                select m.id from ChatMember m
                where m.conversation = c and m.user.userId = :userId
            )
            order by coalesce(c.updatedAt, c.createdAt) desc, c.createdAt desc
            """)
    List<Conversation> findUserConversationsOrderByLatestActivity(@Param("userId") UUID userId);
}
