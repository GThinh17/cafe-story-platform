package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.CafePageRatingResponseDTO;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.CafePageRating;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.mapper.CafePageInteractionMapper;
import com.cafestory.repository.CafePageRatingRepository;
import com.cafestory.service.serviceInterface.CafePageRatingService;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CafePageRatingServiceImpl implements CafePageRatingService {

    private final CafePageRatingRepository cafePageRatingRepository;
    private final CafePageInteractionMapper cafePageInteractionMapper;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public CafePageRatingServiceImpl(
            CafePageRatingRepository cafePageRatingRepository,
            CafePageInteractionMapper cafePageInteractionMapper,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.cafePageRatingRepository = cafePageRatingRepository;
        this.cafePageInteractionMapper = cafePageInteractionMapper;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public CafePageRatingResponseDTO rateCafePage(UUID cafePageId, UUID userId, Integer rating) {
        validateRating(rating);
        CafePage cafePage = validateActiveCafePage(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        CafePageRating cafePageRating = cafePageRatingRepository.findByUserUserIdAndCafePageId(userId, cafePageId)
                .orElseGet(() -> createRating(cafePage, user));
        cafePageRating.setRating(rating);

        CafePageRating savedRating = cafePageRatingRepository.save(cafePageRating);
        return enrich(cafePageInteractionMapper.toCafePageRatingResponseDTO(savedRating), cafePageId);
    }

    @Override
    @Transactional
    public void deleteRating(UUID cafePageId, UUID userId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        CafePageRating cafePageRating = cafePageRatingRepository.findByUserUserIdAndCafePageId(userId, cafePageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page rating not found"));

        cafePageRatingRepository.delete(cafePageRating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageRatingResponseDTO> getRatingsByCafePageId(UUID cafePageId) {
        cafePageValidator.validateCafePageExists(cafePageId);
        long ratingCount = cafePageRatingRepository.countByCafePageId(cafePageId);
        double ratingAverage = averageRating(cafePageId);
        return cafePageRatingRepository.findByCafePageId(cafePageId)
                .stream()
                .map(cafePageInteractionMapper::toCafePageRatingResponseDTO)
                .map(response -> enrich(response, ratingAverage, ratingCount))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CafePageRatingResponseDTO> getRatingsByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return cafePageRatingRepository.findByUserUserId(userId)
                .stream()
                .map(cafePageInteractionMapper::toCafePageRatingResponseDTO)
                .map(response -> enrich(response, response.getCafePageId()))
                .toList();
    }

    private CafePageRating createRating(CafePage cafePage, User user) {
        CafePageRating cafePageRating = new CafePageRating();
        cafePageRating.setCafePage(cafePage);
        cafePageRating.setUser(user);
        return cafePageRating;
    }

    private CafePage validateActiveCafePage(UUID cafePageId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        if (cafePage.getStatus() != PageStatus.ACTIVE || !Boolean.TRUE.equals(cafePage.getPageActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cafe page is not available for rating");
        }
        return cafePage;
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating is required");
        }
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
    }

    private CafePageRatingResponseDTO enrich(CafePageRatingResponseDTO response, UUID cafePageId) {
        return enrich(response, averageRating(cafePageId), cafePageRatingRepository.countByCafePageId(cafePageId));
    }

    private CafePageRatingResponseDTO enrich(
            CafePageRatingResponseDTO response,
            Double ratingAverage,
            Long ratingCount) {
        response.setRatingAverage(ratingAverage);
        response.setRatingCount(ratingCount);
        return response;
    }

    private double averageRating(UUID cafePageId) {
        Double average = cafePageRatingRepository.findAverageRatingByCafePageId(cafePageId);
        return average == null ? 0.0 : average;
    }
}
