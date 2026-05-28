package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogCursorPageResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CafePageService {
    CafePageResponseDTO createCafePage(CafePageCreateDTO cafePageCreateDTO);

    List<CafePageResponseDTO> getAllCafePages();

    List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId);

    CafePageResponseDTO getCafePageById(UUID cafePageId);

    BlogCursorPageResponseDTO getBlogsByCafePageId(UUID cafePageId, String cursor, int size);

    CafePageResponseDTO updateCafePage(UUID cafePageId, UUID actorUserId, CafePageUpdateDTO cafePageUpdateDTO);

    void deleteCafePage(UUID cafePageId, UUID actorUserId);
}
