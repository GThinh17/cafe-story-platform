package com.cafestory.service;

import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.entity.AdFee;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Payment;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdStatus;
import com.cafestory.entity.enums.PaymentStatus;
import com.cafestory.mapper.AdCampaignMapper;
import com.cafestory.repository.AdCampaignRepository;
import com.cafestory.repository.AdClickRepository;
import com.cafestory.repository.AdDailyStatRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.PaymentRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceImplement.AdCampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdCampaignServiceImplTest {

    private final UUID buyerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID paymentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID cafePageId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID campaignId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private AdCampaignRepository adCampaignRepository;

    @Mock
    private AdClickRepository adClickRepository;

    @Mock
    private AdDailyStatRepository adDailyStatRepository;

    @Mock
    private CafePageRepository cafePageRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    private AdCampaignServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdCampaignServiceImpl(
                adCampaignRepository,
                adClickRepository,
                adDailyStatRepository,
                cafePageRepository,
                paymentRepository,
                userRepository,
                new AdCampaignMapper());
    }

    @Test
    void createCampaign_success_paidAdPaymentCanActivateNow_TC001() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(adPayment(PaymentStatus.PAID)));
        when(adCampaignRepository.existsByPaymentPaymentId(paymentId)).thenReturn(false);
        when(cafePageRepository.findById(cafePageId)).thenReturn(Optional.of(cafePage()));
        doAnswer(invocation -> {
            com.cafestory.entity.AdCampaign campaign = invocation.getArgument(0);
            campaign.setAdCampaignId(campaignId);
            return campaign;
        }).when(adCampaignRepository).save(any());

        AdCampaignResponseDTO result = service.createCampaign(request(true));

        assertThat(result.getAdCampaignId()).isEqualTo(campaignId);
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getCafePageId()).isEqualTo(cafePageId);
        assertThat(result.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(result.getStartAt()).isNotNull();
        assertThat(result.getEndAt()).isAfter(result.getStartAt());
    }

    @Test
    void createCampaign_fail_requiresPaidAdPayment_TC002() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(adPayment(PaymentStatus.PENDING)));

        assertThatThrownBy(() -> service.createCampaign(request(false)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private CreateAdCampaignRequestDTO request(boolean activateNow) {
        CreateAdCampaignRequestDTO request = new CreateAdCampaignRequestDTO();
        request.setPaymentId(paymentId);
        request.setCafePageId(cafePageId);
        request.setTitle("Feed ad");
        request.setActivateNow(activateNow);
        return request;
    }

    private Payment adPayment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBuyer(user());
        payment.setAdFee(new AdFee());
        payment.setPaymentStatus(status);
        return payment;
    }

    private CafePage cafePage() {
        CafePage cafePage = new CafePage();
        cafePage.setId(cafePageId);
        cafePage.setOwner(user());
        return cafePage;
    }

    private User user() {
        User user = new User();
        user.setUserId(buyerId);
        return user;
    }
}
