package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.ExtraFeeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtraFeeRequestDTO {

    @NotBlank(message = "Extra fee name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "Extra fee type is mandatory")
    private ExtraFeeType feeType;

    @NotNull(message = "Extra fee price is mandatory")
    @Min(value = 0, message = "Extra fee price must be greater than or equal to 0")
    private Long price;

    @Min(value = 1, message = "Duration months must be greater than or equal to 1")
    private Integer durationMonths;

    @Min(value = 1, message = "Max members must be greater than or equal to 1")
    private Integer maxMembers;

    private Boolean status;
}
