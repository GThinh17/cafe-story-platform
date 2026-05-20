package com.cafestory.repository;

import com.cafestory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUserEmail(String userEmail);

    boolean existsByUserName(String userName);

    Optional<User> findByUserName(String userName);

    Optional<User> findByUserEmail(String userEmail);

    Optional<User> findByUserEmailOrUserName(String userEmail, String userName);
}
