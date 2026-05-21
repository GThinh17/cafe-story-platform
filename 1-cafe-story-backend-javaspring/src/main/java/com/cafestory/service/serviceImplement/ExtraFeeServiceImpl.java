package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.repository.ExtraFeeRepository;
import com.cafestory.service.serviceInterface.ExtraFeeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ExtraFeeServiceImpl implements ExtraFeeService {

    private final ExtraFeeRepository extraFeeRepository;

    public ExtraFeeServiceImpl(ExtraFeeRepository extraFeeRepository) {
        this.extraFeeRepository = extraFeeRepository;
    }

    @Override
    @Transactional
    public ExtraFeeResponseDTO createExtraFee(ExtraFeeRequestDTO request) {
        ExtraFee extraFee = new ExtraFee();
        applyRequest(extraFee, request);
        extraFee.setStatus(request.getStatus() == null ? true : request.getStatus());
        return toResponse(extraFeeRepository.save(extraFee));
    }

    @Override
    @Transactional
    public ExtraFeeResponseDTO updateExtraFee(UUID extraFeeId, ExtraFeeRequestDTO request) {
        ExtraFee extraFee = extraFeeRepository.findById(extraFeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found"));
        Boolean existingStatus = extraFee.getStatus();
        applyRequest(extraFee, request);
        extraFee.setStatus(request.getStatus() == null ? existingStatus : request.getStatus());
        return toResponse(extraFeeRepository.save(extraFee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtraFeeResponseDTO> getAllExtraFees() {
        return extraFeeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyRequest(ExtraFee extraFee, ExtraFeeRequestDTO request) {
        extraFee.setName(request.getName());
        extraFee.setDescription(request.getDescription());
        extraFee.setFeeType(request.getFeeType());
        extraFee.setPrice(request.getPrice());
        extraFee.setDurationMonths(request.getDurationMonths());
    }

    private ExtraFeeResponseDTO toResponse(ExtraFee extraFee) {
        ExtraFeeResponseDTO response = new ExtraFeeResponseDTO();
        response.setExtraFeeId(extraFee.getExtraFeeId());
        response.setName(extraFee.getName());
        response.setDescription(extraFee.getDescription());
        response.setFeeType(extraFee.getFeeType());
        response.setPrice(extraFee.getPrice());
        response.setDurationMonths(extraFee.getDurationMonths());
        response.setStatus(extraFee.getStatus());
        response.setCreatedAt(extraFee.getCreatedAt());
        response.setUpdatedAt(extraFee.getUpdatedAt());
        return response;
    }
}
