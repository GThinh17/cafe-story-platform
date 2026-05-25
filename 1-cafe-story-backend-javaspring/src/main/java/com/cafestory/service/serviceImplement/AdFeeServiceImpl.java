package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdFeeRequestDTO;
import com.cafestory.dto.responseDTO.AdFeeResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.repository.AdFeeRepository;
import com.cafestory.service.serviceInterface.AdFeeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AdFeeServiceImpl implements AdFeeService {

    private final AdFeeRepository adFeeRepository;

    public AdFeeServiceImpl(AdFeeRepository adFeeRepository) {
        this.adFeeRepository = adFeeRepository;
    }

    @Override
    @Transactional
    public AdFeeResponseDTO createAdFee(AdFeeRequestDTO request) {
        AdFee adFee = new AdFee();
        adFee.setFeeType(request.getFeeType());
        adFee.setPrice(request.getPrice());
        adFee.setCurrency(request.getCurrency() == null || request.getCurrency().isBlank() ? "VND" : request.getCurrency());
        adFee.setStatus(request.getStatus() == null ? true : request.getStatus());
        return toResponse(adFeeRepository.save(adFee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdFeeResponseDTO> getAllAdFees() {
        return adFeeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAdFee(UUID adFeeId) {
        AdFee adFee = adFeeRepository.findById(adFeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad fee not found"));
        adFeeRepository.delete(adFee);
    }

    private AdFeeResponseDTO toResponse(AdFee adFee) {
        AdFeeResponseDTO response = new AdFeeResponseDTO();
        response.setAdFeeId(adFee.getAdFeeId());
        response.setFeeType(adFee.getFeeType());
        response.setPrice(adFee.getPrice());
        response.setCurrency(adFee.getCurrency());
        response.setStatus(adFee.getStatus());
        response.setCreatedAt(adFee.getCreatedAt());
        response.setUpdatedAt(adFee.getUpdatedAt());
        return response;
    }
}
