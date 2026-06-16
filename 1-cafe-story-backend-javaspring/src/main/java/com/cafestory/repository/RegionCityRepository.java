package com.cafestory.repository;

import com.cafestory.entity.RegionCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionCityRepository extends JpaRepository<RegionCity, String> {

    List<RegionCity> findByProvinceProvinceCodeOrderByNameAsc(String provinceCode);

    Optional<RegionCity> findByNameIgnoreCase(String name);

    Optional<RegionCity> findByProvinceProvinceCodeAndNameIgnoreCase(String provinceCode, String name);
}
