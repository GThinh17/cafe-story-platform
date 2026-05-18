package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.CafePageCreateDTO;
import com.cafestory.dto.requestDTO.CafePageUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.mapper.CafePageMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.service.serviceInterface.CafePageService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CafePageServiceImpl implements CafePageService {

    private final CafePageRepository cafePageRepository;
    private final BlogRepository blogRepository;
    private final CafePageMapper cafePageMapper;
    private final BlogMapper blogMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public CafePageServiceImpl(
            CafePageRepository cafePageRepository,
            BlogRepository blogRepository,
            CafePageMapper cafePageMapper,
            BlogMapper blogMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.cafePageRepository = cafePageRepository;
        this.blogRepository = blogRepository;
        this.cafePageMapper = cafePageMapper;
        this.blogMapper = blogMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public CafePageResponseDTO createCafePage(CafePageCreateDTO cafePageCreateDTO) {
        User owner = userValidator.validateUserExists(cafePageCreateDTO.getOwnerUserId());
        userValidator.validateUserActive(owner);

        CafePage cafePage = cafePageMapper.toCafePage(cafePageCreateDTO);
        cafePage.setOwner(owner);

        CafePage savedCafePage = cafePageRepository.save(cafePage);
        return cafePageMapper.toCafePageResponseDTO(savedCafePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getAllCafePages() {
        return cafePageRepository.findAll()
                .stream()
                .map(cafePageMapper::toCafePageResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageResponseDTO> getCafePagesByOwnerId(UUID ownerUserId) {
        userValidator.validateUserExists(ownerUserId);
        return cafePageRepository.findByOwnerUserId(ownerUserId)
                .stream()
                .map(cafePageMapper::toCafePageResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CafePageResponseDTO getCafePageById(UUID cafePageId) {
        return cafePageMapper.toCafePageResponseDTO(cafePageValidator.validateCafePageExists(cafePageId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByCafePageId(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        return blogRepository.findByPageId(cafePageId)
                .stream()
                .map(blogMapper::toBlogResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public CafePageResponseDTO updateCafePage(UUID cafePageId, CafePageUpdateDTO cafePageUpdateDTO) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);

        if (cafePageUpdateDTO.getRegionId() != null) {
            cafePage.setRegionId(cafePageUpdateDTO.getRegionId());
        }
        if (cafePageUpdateDTO.getName() != null) {
            cafePage.setName(cafePageUpdateDTO.getName());
        }
        if (cafePageUpdateDTO.getAddress() != null) {
            cafePage.setAddress(cafePageUpdateDTO.getAddress());
        }
        if (cafePageUpdateDTO.getDescription() != null) {
            cafePage.setDescription(cafePageUpdateDTO.getDescription());
        }
        if (cafePageUpdateDTO.getAvatarUrl() != null) {
            cafePage.setAvatarUrl(cafePageUpdateDTO.getAvatarUrl());
        }
        if (cafePageUpdateDTO.getCoverUrl() != null) {
            cafePage.setCoverUrl(cafePageUpdateDTO.getCoverUrl());
        }
        if (cafePageUpdateDTO.getStatus() != null) {
            cafePage.setStatus(cafePageUpdateDTO.getStatus());
        }

        CafePage updatedCafePage = cafePageRepository.save(cafePage);
        return cafePageMapper.toCafePageResponseDTO(updatedCafePage);
    }

    @Override
    @Transactional
    public void deleteCafePage(UUID cafePageId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        cafePageRepository.delete(cafePage);
    }
}
