package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminExtraFeeStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.enums.ExtraFeeType;
import com.cafestory.repository.ExtraFeeRepository;
import com.cafestory.service.serviceInterface.AdminExtraFeeService;
import com.cafestory.service.serviceInterface.ExtraFeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminExtraFeeServiceImpl implements AdminExtraFeeService {

    private final ExtraFeeRepository extraFeeRepository;
    private final ExtraFeeService extraFeeService;

    public AdminExtraFeeServiceImpl(ExtraFeeRepository extraFeeRepository, ExtraFeeService extraFeeService) {
        this.extraFeeRepository = extraFeeRepository;
        this.extraFeeService = extraFeeService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExtraFeeResponseDTO> getExtraFees(Boolean status, Pageable pageable) {
        Page<ExtraFee> extraFees = status == null
                ? extraFeeRepository.findAll(pageable)
                : extraFeeRepository.findByStatus(status, pageable);
        return extraFees.map(this::toResponse);
    }

    @Override
    public ExtraFeeResponseDTO createExtraFee(ExtraFeeRequestDTO request) {
        return extraFeeService.createExtraFee(request);
    }

    @Override
    public ExtraFeeResponseDTO updateExtraFee(UUID extraFeeId, ExtraFeeRequestDTO request) {
        return extraFeeService.updateExtraFee(extraFeeId, request);
    }

    @Override
    @Transactional
    public ExtraFeeResponseDTO updateExtraFeeStatus(UUID extraFeeId, AdminExtraFeeStatusUpdateRequestDTO request) {
        ExtraFee extraFee = extraFeeRepository.findById(extraFeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));
        extraFee.setStatus(request.getStatus());
        return toResponse(extraFeeRepository.save(extraFee));
    }

    @Override
    public void deleteExtraFee(UUID extraFeeId) {
        extraFeeService.deleteExtraFee(extraFeeId);
    }

    private ExtraFeeResponseDTO toResponse(ExtraFee extraFee) {
        ExtraFeeResponseDTO response = new ExtraFeeResponseDTO();
        response.setExtraFeeId(extraFee.getExtraFeeId());
        response.setName(extraFee.getName());
        response.setDescription(extraFee.getDescription());
        response.setFeeType(extraFee.getFeeType());
        response.setPrice(extraFee.getPrice());
        response.setDurationMonths(extraFee.getDurationMonths());
        response.setMaxMembers(resolveMaxMembers(extraFee));
        response.setStatus(extraFee.getStatus());
        response.setCreatedAt(extraFee.getCreatedAt());
        response.setUpdatedAt(extraFee.getUpdatedAt());
        return response;
    }

    private Integer resolveMaxMembers(ExtraFee extraFee) {
        if (extraFee.getFeeType() == ExtraFeeType.CAFE_PAGE_OPENING && extraFee.getMaxMembers() == null) {
            return 2;
        }
        return extraFee.getMaxMembers();
    }
}
