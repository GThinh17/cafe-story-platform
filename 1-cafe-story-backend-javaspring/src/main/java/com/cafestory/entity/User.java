package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.cafestory.entity.enums.UserRole;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole = UserRole.USER;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "ward")
    private String ward;

    @Column(name = "country")
    private String country;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
