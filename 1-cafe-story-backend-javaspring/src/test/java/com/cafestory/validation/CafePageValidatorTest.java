package com.cafestory.validation;

import com.cafestory.entity.CafePage;
import com.cafestory.repository.CafePageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafePageValidatorTest {

    @Mock
    private CafePageRepository cafePageRepository;

    @InjectMocks
    private CafePageValidator cafePageValidator;

    @Test
    void validateCafePageExists_success_TC001() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());

        when(cafePageRepository.findById(cafePage.getId())).thenReturn(Optional.of(cafePage));

        CafePage result = cafePageValidator.validateCafePageExists(cafePage.getId());

        assertThat(result).isEqualTo(cafePage);
    }

    @Test
    void validateCafePageExists_fail_nullCafePageId_TC002() {
        assertThatThrownBy(() -> cafePageValidator.validateCafePageExists(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validateCafePageExists_fail_cafePageNotFound_TC003() {
        UUID cafePageId = UUID.randomUUID();

        when(cafePageRepository.findById(cafePageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cafePageValidator.validateCafePageExists(cafePageId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
