package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.PayoutFormulaRequest;
import com.cafestory.dto.responseDTO.payout.PayoutFormulaResponse;
import com.cafestory.entity.PayoutFormula;
import com.cafestory.entity.User;
import com.cafestory.repository.PayoutFormulaRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.PayoutFormulaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PayoutFormulaServiceImpl implements PayoutFormulaService {

    private final PayoutFormulaRepository formulaRepository;
    private final UserRepository userRepository;

    public PayoutFormulaServiceImpl(PayoutFormulaRepository formulaRepository,
                                    UserRepository userRepository) {
        this.formulaRepository = formulaRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PayoutFormula getActiveFormula() {
        return formulaRepository.findByActiveTrue()
                .orElseGet(this::createDefaultActiveFormula);
    }

    @Override
    @Transactional
    public PayoutFormulaResponse createFormula(UUID adminUserId, PayoutFormulaRequest request) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        PayoutFormula formula = buildFromRequest(request);
        formula.setCreatedBy(admin);
        formula.setActive(false);
        return toResponse(formulaRepository.save(formula));
    }

    @Override
    @Transactional
    public PayoutFormulaResponse activateFormula(UUID adminUserId, UUID formulaId) {
        PayoutFormula target = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout formula not found"));
        formulaRepository.findByActiveTrue().ifPresent(current -> {
            if (!current.getId().equals(formulaId)) {
                current.setActive(false);
                formulaRepository.save(current);
            }
        });
        target.setActive(true);
        return toResponse(formulaRepository.save(target));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutFormulaResponse> getAllFormulas() {
        return formulaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PayoutFormula createDefaultActiveFormula() {
        PayoutFormula formula = new PayoutFormula();
        formula.setLikePayoutAmount(100L);
        formula.setCommentPayoutAmount(500L);
        formula.setSharePayoutAmount(300L);
        formula.setIronMultiplier(BigDecimal.ONE);
        formula.setBronzeMultiplier(new BigDecimal("1.20"));
        formula.setSilverMultiplier(new BigDecimal("1.50"));
        formula.setGoldMultiplier(new BigDecimal("2.00"));
        formula.setDiamondMultiplier(new BigDecimal("3.00"));
        formula.setActive(true);
        formula.setDescription("Default payout formula");
        return formulaRepository.save(formula);
    }

    private PayoutFormula buildFromRequest(PayoutFormulaRequest request) {
        PayoutFormula formula = new PayoutFormula();
        formula.setLikePayoutAmount(request.getLikePayoutAmount());
        formula.setCommentPayoutAmount(request.getCommentPayoutAmount());
        formula.setSharePayoutAmount(request.getSharePayoutAmount());
        formula.setIronMultiplier(request.getIronMultiplier());
        formula.setBronzeMultiplier(request.getBronzeMultiplier());
        formula.setSilverMultiplier(request.getSilverMultiplier());
        formula.setGoldMultiplier(request.getGoldMultiplier());
        formula.setDiamondMultiplier(request.getDiamondMultiplier());
        formula.setDescription(request.getDescription());
        return formula;
    }

    public PayoutFormulaResponse toResponse(PayoutFormula formula) {
        PayoutFormulaResponse dto = new PayoutFormulaResponse();
        dto.setId(formula.getId());
        dto.setLikePayoutAmount(formula.getLikePayoutAmount());
        dto.setCommentPayoutAmount(formula.getCommentPayoutAmount());
        dto.setSharePayoutAmount(formula.getSharePayoutAmount());
        dto.setIronMultiplier(formula.getIronMultiplier());
        dto.setBronzeMultiplier(formula.getBronzeMultiplier());
        dto.setSilverMultiplier(formula.getSilverMultiplier());
        dto.setGoldMultiplier(formula.getGoldMultiplier());
        dto.setDiamondMultiplier(formula.getDiamondMultiplier());
        dto.setActive(formula.isActive());
        dto.setDescription(formula.getDescription());
        dto.setCreatedAt(formula.getCreatedAt());
        return dto;
    }
}
