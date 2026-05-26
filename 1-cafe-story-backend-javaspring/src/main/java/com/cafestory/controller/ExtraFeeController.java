package com.cafestory.controller;

import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.service.serviceInterface.ExtraFeeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/extra-fees")
public class ExtraFeeController {

    private final ExtraFeeService extraFeeService;

    public ExtraFeeController(ExtraFeeService extraFeeService) {
        this.extraFeeService = extraFeeService;
    }

    @GetMapping
    public List<ExtraFeeResponseDTO> getActiveExtraFees() {
        return extraFeeService.getActiveExtraFees();
    }
}
