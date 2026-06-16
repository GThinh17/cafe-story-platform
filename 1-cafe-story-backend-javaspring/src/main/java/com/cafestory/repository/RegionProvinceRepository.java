package com.cafestory.repository;

import com.cafestory.entity.RegionProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionProvinceRepository extends JpaRepository<RegionProvince, String> {

    List<RegionProvince> findAllByOrderByNameAsc();

    Optional<RegionProvince> findByNameIgnoreCase(String name);
}
