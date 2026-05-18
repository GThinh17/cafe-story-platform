package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.repository.CafePageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class CafePageValidator {

    private final CafePageRepository cafePageRepository;

    public CafePageValidator(CafePageRepository cafePageRepository) {
        this.cafePageRepository = cafePageRepository;
    }

    public CafePage validateCafePageExists(UUID cafePageId) {
        validateCafePageIdNotNull(cafePageId);
        return cafePageRepository.findById(cafePageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page not found"));
    }

    private void validateCafePageIdNotNull(UUID cafePageId) {
        if (cafePageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cafe page id is required");
        }
    }
}
