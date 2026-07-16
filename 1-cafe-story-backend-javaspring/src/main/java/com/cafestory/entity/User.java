package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @NotBlank(message = "Username is mandatory")
    @Size(max = 255)
    @Column(name = "user_name")
    private String userName;

    @Size(max = 255)
    @Column(name = "user_full_name")
    private String userFullName;

    @NotBlank(message = "Password is mandatory")
    @Size(max = 255)
    @Column(name = "user_password")
    private String userPassword;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    @Column(name = "user_email", unique = true)
    private String userEmail;

    @Column(name = "user_phone")
    private Long userPhone;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Column(name = "user_description", columnDefinition = "TEXT")
    private String userDescription;

    @Column(name = "user_like", nullable = false)
    @ColumnDefault("0")
    private Integer userLike = 0;

    @Column(name = "user_follower", nullable = false)
    @ColumnDefault("0")
    private Integer userFollower = 0;

    @NotNull
    @Column(name = "account_status", nullable = false)
    @ColumnDefault("true")
    private Boolean accountStatus = true;

    @NotNull
    @Column(name = "hide_cafe_page_on_profile", nullable = false)
    @ColumnDefault("false")
    private Boolean hideCafePageOnProfile = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
}
