package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.AdFeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdFeeRequestDTO {

    @NotNull(message = "Ad fee type is mandatory")
    private AdFeeType feeType;

    @NotNull(message = "Ad fee price is mandatory")
    @DecimalMin(value = "0.01", message = "Ad fee price must be greater than 0")
    private BigDecimal price;

    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency = "VND";

    private Boolean status;
}
