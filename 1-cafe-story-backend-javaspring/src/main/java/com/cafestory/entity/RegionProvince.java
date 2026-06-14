package com.cafestory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "region_provinces")
public class RegionProvince {

    @Id
    @Column(name = "province_code", length = 16, nullable = false)
    private String provinceCode;

    @Column(name = "name", nullable = false)
    private String name;
}
