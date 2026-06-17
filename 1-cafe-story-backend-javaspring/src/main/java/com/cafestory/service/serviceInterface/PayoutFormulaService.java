package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.PayoutFormulaRequest;
import com.cafestory.dto.responseDTO.payout.PayoutFormulaResponse;
import com.cafestory.entity.PayoutFormula;

import java.util.List;
import java.util.UUID;

public interface PayoutFormulaService {

    PayoutFormula getActiveFormula();

    PayoutFormulaResponse createFormula(UUID adminUserId, PayoutFormulaRequest request);

    PayoutFormulaResponse activateFormula(UUID adminUserId, UUID formulaId);

    List<PayoutFormulaResponse> getAllFormulas();
}
