package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.service.serviceInterface.AdminCafePageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminCafePageServiceImpl implements AdminCafePageService {

    private final CafePageRepository cafePageRepository;
    private final CafePageMapper cafePageMapper;
    private final CafePageRatingRepository cafePageRatingRepository;

    public AdminCafePageServiceImpl(CafePageRepository cafePageRepository,
            CafePageMapper cafePageMapper,
            CafePageRatingRepository cafePageRatingRepository) {
        this.cafePageRepository = cafePageRepository;
        this.cafePageMapper = cafePageMapper;
        this.cafePageRatingRepository = cafePageRatingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CafePageResponseDTO> getCafePages(PageStatus status, UUID ownerUserId, Pageable pageable) {
        return cafePageRepository.findAdminCafePages(status, ownerUserId, pageable)
                .map(this::toCafePageResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CafePageResponseDTO getCafePage(UUID pageId) {
        return toCafePageResponseDTO(findCafePage(pageId));
    }

    @Override
    @Transactional
    public CafePageResponseDTO updateCafePageStatus(UUID pageId, AdminCafePageStatusUpdateRequestDTO request) {
        CafePage cafePage = findCafePage(pageId);
        cafePage.setStatus(request.getStatus());
        cafePage.setPageActive(request.getStatus() == PageStatus.ACTIVE);
        return toCafePageResponseDTO(cafePageRepository.save(cafePage));
    }

    @Override
    @Transactional
    public void deleteCafePage(UUID pageId) {
        CafePage cafePage = findCafePage(pageId);
        cafePageRepository.delete(cafePage);
    }

    private CafePage findCafePage(UUID pageId) {
        return cafePageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page not found"));
    }

    private CafePageResponseDTO toCafePageResponseDTO(CafePage cafePage) {
        CafePageResponseDTO response = cafePageMapper.toCafePageResponseDTO(cafePage);
        response.setIsFollowing(false);
        response.setIsLiked(false);
        response.setIsRating(false);
        response.setMyRating(null);
        response.setRatingScore(cafePageRatingRepository.findAverageRatingByCafePageId(cafePage.getId()));
        response.setRatingCount(cafePageRatingRepository.countByCafePageId(cafePage.getId()));
        return response;
    }
}
