package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class PageMemberId implements Serializable {

    @Column(name = "page_id")
    private UUID pageId;

    @Column(name = "user_id")
    private UUID userId;
}
