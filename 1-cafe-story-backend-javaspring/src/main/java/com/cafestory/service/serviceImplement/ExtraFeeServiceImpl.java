package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.enums.ExtraFeeType;
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

    private static final int DEFAULT_CAFE_PAGE_MAX_MEMBERS = 2;

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

    @Override
    @Transactional
    public void deleteExtraFee(UUID extraFeeId) {
        if (!extraFeeRepository.existsById(extraFeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra fee not found");
        }
        extraFeeRepository.deleteById(extraFeeId);
    }

    private void applyRequest(ExtraFee extraFee, ExtraFeeRequestDTO request) {
        validateMaxMembers(request.getMaxMembers());
        extraFee.setName(request.getName());
        extraFee.setDescription(request.getDescription());
        extraFee.setFeeType(request.getFeeType());
        extraFee.setPrice(request.getPrice());
        extraFee.setDurationMonths(request.getDurationMonths());
        extraFee.setMaxMembers(resolveMaxMembers(request.getFeeType(), request.getMaxMembers()));
    }

    private void validateMaxMembers(Integer maxMembers) {
        if (maxMembers != null && maxMembers < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max members must be greater than or equal to 1");
        }
    }

    private Integer resolveMaxMembers(ExtraFeeType feeType, Integer maxMembers) {
        if (feeType == ExtraFeeType.CAFE_PAGE_OPENING) {
            return maxMembers == null ? DEFAULT_CAFE_PAGE_MAX_MEMBERS : maxMembers;
        }
        return maxMembers;
    }

    private ExtraFeeResponseDTO toResponse(ExtraFee extraFee) {
        ExtraFeeResponseDTO response = new ExtraFeeResponseDTO();
        response.setExtraFeeId(extraFee.getExtraFeeId());
        response.setName(extraFee.getName());
        response.setDescription(extraFee.getDescription());
        response.setFeeType(extraFee.getFeeType());
        response.setPrice(extraFee.getPrice());
        response.setDurationMonths(extraFee.getDurationMonths());
        response.setMaxMembers(extraFee.getMaxMembers());
        response.setStatus(extraFee.getStatus());
        response.setCreatedAt(extraFee.getCreatedAt());
        response.setUpdatedAt(extraFee.getUpdatedAt());
        return response;
    }
}
