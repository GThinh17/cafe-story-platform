package com.cafestory.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class UserRoleId implements Serializable {
    private UUID user;
    private Integer role;
}
