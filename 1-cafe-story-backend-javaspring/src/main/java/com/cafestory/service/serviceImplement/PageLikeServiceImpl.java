package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.PageLikeResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.PageLike;
import com.cafestory.entity.User;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.PageLikeRepository;
import com.cafestory.service.serviceInterface.PageLikeService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PageLikeServiceImpl implements PageLikeService {

    private final PageLikeRepository pageLikeRepository;
    private final CafePageInteractionMapper cafePageInteractionMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public PageLikeServiceImpl(
            PageLikeRepository pageLikeRepository,
            CafePageInteractionMapper cafePageInteractionMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.pageLikeRepository = pageLikeRepository;
        this.cafePageInteractionMapper = cafePageInteractionMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true)
    })
    public PageLikeResponseDTO likePage(UUID cafePageId, UUID userId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        if (pageLikeRepository.existsByUserUserIdAndCafePageId(userId, cafePageId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cafe page already liked by user");
        }

        PageLike pageLike = new PageLike();
        pageLike.setCafePage(cafePage);
        pageLike.setUser(user);

        PageLike savedPageLike = pageLikeRepository.save(pageLike);
        incrementLikeCount(cafePage);
        return cafePageInteractionMapper.toPageLikeResponseDTO(savedPageLike);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.CAFE_PAGE_DETAIL_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true)
    })
    public void unlikePage(UUID cafePageId, UUID userId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        PageLike pageLike = pageLikeRepository.findByUserUserIdAndCafePageId(userId, cafePageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page like not found"));

        pageLikeRepository.delete(pageLike);
        decrementLikeCount(pageLike.getCafePage());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageLikeResponseDTO> getLikesByCafePageId(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        return pageLikeRepository.findByCafePageId(cafePageId)
                .stream()
                .map(cafePageInteractionMapper::toPageLikeResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageLikeResponseDTO> getLikesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return pageLikeRepository.findByUserUserId(userId)
                .stream()
                .map(cafePageInteractionMapper::toPageLikeResponseDTO)
                .toList();
    }

    private void incrementLikeCount(CafePage cafePage) {
        int currentCount = cafePage.getLikeCount() == null ? 0 : cafePage.getLikeCount();
        cafePage.setLikeCount(currentCount + 1);
    }

    private void decrementLikeCount(CafePage cafePage) {
        int currentCount = cafePage.getLikeCount() == null ? 0 : cafePage.getLikeCount();
        cafePage.setLikeCount(Math.max(0, currentCount - 1));
    }
}
