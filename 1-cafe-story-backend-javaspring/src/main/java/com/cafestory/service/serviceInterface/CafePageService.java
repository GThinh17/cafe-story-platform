package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CafePageService {
    CafePageResponseDTO createCafePage(CafePageCreateDTO cafePageCreateDTO);

    List<CafePageResponseDTO> getAllCafePages();

    List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId);

    CafePageResponseDTO getCafePageById(UUID cafePageId);

    List<BlogResponseDTO> getBlogsByCafePageId(UUID cafePageId);

    CafePageResponseDTO updateCafePage(UUID cafePageId, CafePageUpdateDTO cafePageUpdateDTO);

    void deleteCafePage(UUID cafePageId);
}
