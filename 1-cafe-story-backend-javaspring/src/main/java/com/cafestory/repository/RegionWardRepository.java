package com.cafestory.repository;

import com.cafestory.entity.RegionWard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionWardRepository extends JpaRepository<RegionWard, String> {

    List<RegionWard> findByProvinceProvinceCodeOrderByNameAsc(String provinceCode);

    List<RegionWard> findByCityCityCodeOrderByNameAsc(String cityCode);

    Optional<RegionWard> findFirstByProvinceProvinceCodeAndNameIgnoreCase(String provinceCode, String name);
}
