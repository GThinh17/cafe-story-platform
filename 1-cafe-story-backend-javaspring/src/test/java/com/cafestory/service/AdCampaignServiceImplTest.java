package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdTargetRegionRequestDTO;
import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdCampaignStatsResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdDailyStat;
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
import com.cafestory.validation.CafePageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AdCampaignServiceImplTest {

    private static final UUID BUYER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PAYMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CAFE_PAGE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CAMPAIGN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock private AdCampaignRepository adCampaignRepository;
    @Mock private AdClickRepository adClickRepository;
    @Mock private AdDailyStatRepository adDailyStatRepository;
    @Mock private CafePageRepository cafePageRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private CafePageValidator cafePageValidator;

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
                new AdCampaignMapper(),
                cafePageValidator);
    }

    @Test
    void createCampaign_success_paidOwnerCanActivateAndPriorityIsServerControlled_TC001() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(adPayment(PaymentStatus.PAID)));
        when(adCampaignRepository.existsByPaymentPaymentId(PAYMENT_ID)).thenReturn(false);
        when(cafePageRepository.findById(CAFE_PAGE_ID)).thenReturn(Optional.of(cafePage(BUYER_ID)));
        saveCampaignAnswer();

        CreateAdCampaignRequestDTO request = request(true);
        AdTargetRegionRequestDTO region = new AdTargetRegionRequestDTO();
        region.setProvince("Ho Chi Minh");
        region.setCity("Thu Duc");
        request.setTargetRegions(List.of(region));

        AdCampaignResponseDTO result = service.createCampaign(BUYER_ID, request);

        assertThat(result.getAdCampaignId()).isEqualTo(CAMPAIGN_ID);
        assertThat(result.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(result.getPriority()).isEqualTo(1);
        assertThat(result.getStartAt()).isNotNull();
        assertThat(result.getEndAt()).isAfter(result.getStartAt());
        assertThat(result.getTargetRegions()).singleElement().satisfies(target -> {
            assertThat(target.getProvince()).isEqualTo("Ho Chi Minh");
            assertThat(target.getCity()).isEqualTo("Thu Duc");
        });
    }

    @Test
    void createCampaign_success_nullRegionsCreatesDraft_TC002() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(adPayment(PaymentStatus.PAID)));
        when(adCampaignRepository.existsByPaymentPaymentId(PAYMENT_ID)).thenReturn(false);
        when(cafePageRepository.findById(CAFE_PAGE_ID)).thenReturn(Optional.of(cafePage(BUYER_ID)));
        saveCampaignAnswer();
        CreateAdCampaignRequestDTO request = request(false);
        request.setTargetRegions(null);

        AdCampaignResponseDTO result = service.createCampaign(BUYER_ID, request);

        assertThat(result.getStatus()).isEqualTo(AdStatus.DRAFT);
        assertThat(result.getTargetRegions()).isEmpty();
    }

    @Test
    void createCampaign_fail_validatesPaymentAndOwnership_TC003() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.NOT_FOUND);

        Payment otherPayment = adPayment(PaymentStatus.PAID);
        otherPayment.setBuyer(user(OTHER_USER_ID));
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(otherPayment));
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.FORBIDDEN);

        otherPayment.setBuyer(null);
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.FORBIDDEN);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(adPayment(PaymentStatus.PENDING)));
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.BAD_REQUEST);

        Payment notAdPayment = adPayment(PaymentStatus.PAID);
        notAdPayment.setAdFee(null);
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(notAdPayment));
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.BAD_REQUEST);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(adPayment(PaymentStatus.PAID)));
        when(adCampaignRepository.existsByPaymentPaymentId(PAYMENT_ID)).thenReturn(true);
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.BAD_REQUEST);
    }

    @Test
    void createCampaign_fail_validatesCafePageOwner_TC004() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(adPayment(PaymentStatus.PAID)));
        when(adCampaignRepository.existsByPaymentPaymentId(PAYMENT_ID)).thenReturn(false);
        when(cafePageRepository.findById(CAFE_PAGE_ID)).thenReturn(Optional.empty());
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.NOT_FOUND);

        when(cafePageRepository.findById(CAFE_PAGE_ID)).thenReturn(Optional.of(cafePage(OTHER_USER_ID)));
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.FORBIDDEN);

        CafePage ownerless = cafePage(OTHER_USER_ID);
        ownerless.setOwner(null);
        when(cafePageRepository.findById(CAFE_PAGE_ID)).thenReturn(Optional.of(ownerless));
        assertStatus(() -> service.createCampaign(BUYER_ID, request(false)), HttpStatus.FORBIDDEN);
    }

    @Test
    void campaignManagement_success_authorizesPageManagers_TC005() {
        AdCampaign campaign = campaign(AdStatus.ACTIVE);
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(adCampaignRepository.findByCafePageIdOrderByCreatedAtDesc(CAFE_PAGE_ID)).thenReturn(List.of(campaign));
        when(adCampaignRepository.save(any(AdCampaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.getCampaign(BUYER_ID, CAMPAIGN_ID).getAdCampaignId()).isEqualTo(CAMPAIGN_ID);
        assertThat(service.getCampaignsByCafePage(BUYER_ID, CAFE_PAGE_ID)).hasSize(1);
        assertThat(service.pauseCampaign(BUYER_ID, CAMPAIGN_ID).getStatus()).isEqualTo(AdStatus.PAUSED);
        assertThat(service.activateCampaign(BUYER_ID, CAMPAIGN_ID).getStatus()).isEqualTo(AdStatus.ACTIVE);

        verify(cafePageValidator, times(4)).validateUserCanManagePage(CAFE_PAGE_ID, BUYER_ID);
    }

    @Test
    void campaignManagement_fail_missingOrInvalidLifecycle_TC006() {
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.empty());
        assertStatus(() -> service.getCampaign(BUYER_ID, CAMPAIGN_ID), HttpStatus.NOT_FOUND);

        AdCampaign draft = campaign(AdStatus.DRAFT);
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(draft));
        assertStatus(() -> service.pauseCampaign(BUYER_ID, CAMPAIGN_ID), HttpStatus.BAD_REQUEST);

        draft.setStatus(AdStatus.REJECTED);
        assertStatus(() -> service.activateCampaign(BUYER_ID, CAMPAIGN_ID), HttpStatus.BAD_REQUEST);
        draft.setStatus(AdStatus.EXPIRED);
        assertStatus(() -> service.activateCampaign(BUYER_ID, CAMPAIGN_ID), HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordClick_success_usesAuthenticatedUserAndUpdatesExistingDailyStat_TC007() {
        AdCampaign campaign = campaign(AdStatus.ACTIVE);
        AdDailyStat stat = dailyStat(campaign, 4, 2);
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.of(user(BUYER_ID)));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(CAMPAIGN_ID, LocalDate.now()))
                .thenReturn(Optional.of(stat));

        service.recordClick(BUYER_ID, CAMPAIGN_ID);

        ArgumentCaptor<com.cafestory.entity.AdClick> clickCaptor = ArgumentCaptor.forClass(com.cafestory.entity.AdClick.class);
        verify(adClickRepository).save(clickCaptor.capture());
        assertThat(clickCaptor.getValue().getUser().getUserId()).isEqualTo(BUYER_ID);
        assertThat(clickCaptor.getValue().getClickedAt()).isNotNull();
        assertThat(stat.getClicks()).isEqualTo(3);
        verify(adDailyStatRepository).save(stat);
    }

    @Test
    void recordClick_success_createsDailyStatAndRejectsMissingUser_TC008() {
        AdCampaign campaign = campaign(AdStatus.ACTIVE);
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.empty());
        assertStatus(() -> service.recordClick(BUYER_ID, CAMPAIGN_ID), HttpStatus.NOT_FOUND);

        when(userRepository.findById(BUYER_ID)).thenReturn(Optional.of(user(BUYER_ID)));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(CAMPAIGN_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        service.recordClick(BUYER_ID, CAMPAIGN_ID);

        ArgumentCaptor<AdDailyStat> statCaptor = ArgumentCaptor.forClass(AdDailyStat.class);
        verify(adDailyStatRepository).save(statCaptor.capture());
        assertThat(statCaptor.getValue().getClicks()).isEqualTo(1);
        assertThat(statCaptor.getValue().getStatDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void getCampaignStats_success_aggregatesDailyDataAndCtr_TC009() {
        AdCampaign campaign = campaign(AdStatus.ACTIVE);
        campaign.setServedImpressions(120);
        campaign.setMaxImpressions(10000);
        campaign.setEndAt(LocalDateTime.now().plusDays(4));
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(adClickRepository.countByAdCampaignAdCampaignId(CAMPAIGN_ID)).thenReturn(8L);
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdOrderByStatDateAsc(CAMPAIGN_ID))
                .thenReturn(List.of(dailyStat(campaign, 120, 8)));

        AdCampaignStatsResponseDTO result = service.getCampaignStats(BUYER_ID, CAMPAIGN_ID);

        assertThat(result.getRemainingImpressions()).isEqualTo(9880);
        assertThat(result.getCtrPercent()).isEqualByComparingTo("6.67");
        assertThat(result.getRemainingDays()).isEqualTo(4);
        assertThat(result.getDailyStats()).singleElement().satisfies(day -> {
            assertThat(day.getImpressions()).isEqualTo(120);
            assertThat(day.getClicks()).isEqualTo(8);
        });
    }

    @Test
    void getCampaignStats_success_handlesDraftNullCountersAndPastEnd_TC010() {
        AdCampaign campaign = campaign(AdStatus.DRAFT);
        campaign.setServedImpressions(null);
        campaign.setMaxImpressions(null);
        campaign.setMaxDurationDays(null);
        campaign.setEndAt(null);
        when(adCampaignRepository.findById(CAMPAIGN_ID)).thenReturn(Optional.of(campaign));
        when(adDailyStatRepository.findByAdCampaignAdCampaignIdOrderByStatDateAsc(CAMPAIGN_ID)).thenReturn(List.of());

        AdCampaignStatsResponseDTO draft = service.getCampaignStats(BUYER_ID, CAMPAIGN_ID);
        assertThat(draft.getCtrPercent()).isEqualByComparingTo("0.00");
        assertThat(draft.getRemainingDays()).isZero();

        campaign.setEndAt(LocalDateTime.now().minusDays(2));
        assertThat(service.getCampaignStats(BUYER_ID, CAMPAIGN_ID).getRemainingDays()).isZero();
    }

    @Test
    void expireActiveCampaigns_success_expiresByImpressionsDurationOrEndAt_TC011() {
        AdCampaign impressions = campaign(AdStatus.ACTIVE);
        impressions.setServedImpressions(impressions.getMaxImpressions());
        AdCampaign duration = campaign(AdStatus.ACTIVE);
        duration.setAdCampaignId(UUID.randomUUID());
        duration.setStartAt(LocalDateTime.now().minusDays(31));
        duration.setEndAt(null);
        AdCampaign endAt = campaign(AdStatus.ACTIVE);
        endAt.setAdCampaignId(UUID.randomUUID());
        endAt.setEndAt(LocalDateTime.now().minusSeconds(1));
        AdCampaign valid = campaign(AdStatus.ACTIVE);
        valid.setAdCampaignId(UUID.randomUUID());
        when(adCampaignRepository.findByStatus(AdStatus.ACTIVE))
                .thenReturn(List.of(impressions, duration, endAt, valid));

        service.expireActiveCampaigns();

        assertThat(impressions.getStatus()).isEqualTo(AdStatus.EXPIRED);
        assertThat(duration.getStatus()).isEqualTo(AdStatus.EXPIRED);
        assertThat(endAt.getStatus()).isEqualTo(AdStatus.EXPIRED);
        assertThat(valid.getStatus()).isEqualTo(AdStatus.ACTIVE);
    }

    private CreateAdCampaignRequestDTO request(boolean activateNow) {
        CreateAdCampaignRequestDTO request = new CreateAdCampaignRequestDTO();
        request.setPaymentId(PAYMENT_ID);
        request.setCafePageId(CAFE_PAGE_ID);
        request.setTitle("Feed ad");
        request.setDescription("A focused sponsored campaign");
        request.setImageUrl("https://cdn.example.com/ad.jpg");
        request.setTargetUrl("https://cafestory.example/cafes/bean-house");
        request.setActivateNow(activateNow);
        return request;
    }

    private Payment adPayment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setBuyer(user(BUYER_ID));
        payment.setAdFee(new AdFee());
        payment.setPaymentStatus(status);
        return payment;
    }

    private CafePage cafePage(UUID ownerId) {
        CafePage cafePage = new CafePage();
        cafePage.setId(CAFE_PAGE_ID);
        cafePage.setOwner(user(ownerId));
        return cafePage;
    }

    private AdCampaign campaign(AdStatus status) {
        AdCampaign campaign = new AdCampaign();
        campaign.setAdCampaignId(CAMPAIGN_ID);
        campaign.setCafePage(cafePage(BUYER_ID));
        campaign.setPayment(adPayment(PaymentStatus.PAID));
        campaign.setTitle("Feed ad");
        campaign.setStatus(status);
        campaign.setPriority(1);
        campaign.setMaxImpressions(10000);
        campaign.setServedImpressions(0);
        campaign.setMaxDurationDays(30);
        campaign.setStartAt(LocalDateTime.now().minusDays(1));
        campaign.setEndAt(LocalDateTime.now().plusDays(29));
        return campaign;
    }

    private AdDailyStat dailyStat(AdCampaign campaign, int impressions, int clicks) {
        AdDailyStat stat = new AdDailyStat();
        stat.setAdCampaign(campaign);
        stat.setStatDate(LocalDate.now());
        stat.setImpressions(impressions);
        stat.setClicks(clicks);
        return stat;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    private void saveCampaignAnswer() {
        doAnswer(invocation -> {
            AdCampaign campaign = invocation.getArgument(0);
            campaign.setAdCampaignId(CAMPAIGN_ID);
            return campaign;
        }).when(adCampaignRepository).save(any(AdCampaign.class));
    }

    private void assertStatus(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, HttpStatus status) {
        assertThatThrownBy(action)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(status));
    }
}
