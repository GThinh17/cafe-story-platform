package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdTargetRegionRequestDTO;
import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
import com.cafestory.dto.responseDTO.AdCampaignStatsResponseDTO;
import com.cafestory.dto.responseDTO.AdDailyStatResponseDTO;
import com.cafestory.entity.AdCampaign;
import com.cafestory.entity.AdClick;
import com.cafestory.entity.AdDailyStat;
import com.cafestory.entity.AdTargetRegion;
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
import com.cafestory.service.serviceInterface.AdCampaignService;
import com.cafestory.validation.CafePageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AdCampaignServiceImpl implements AdCampaignService {

    private final AdCampaignRepository adCampaignRepository;
    private final AdClickRepository adClickRepository;
    private final AdDailyStatRepository adDailyStatRepository;
    private final CafePageRepository cafePageRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AdCampaignMapper adCampaignMapper;
    private final CafePageValidator cafePageValidator;

    public AdCampaignServiceImpl(
            AdCampaignRepository adCampaignRepository,
            AdClickRepository adClickRepository,
            AdDailyStatRepository adDailyStatRepository,
            CafePageRepository cafePageRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            AdCampaignMapper adCampaignMapper,
            CafePageValidator cafePageValidator) {
        this.adCampaignRepository = adCampaignRepository;
        this.adClickRepository = adClickRepository;
        this.adDailyStatRepository = adDailyStatRepository;
        this.cafePageRepository = cafePageRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.adCampaignMapper = adCampaignMapper;
        this.cafePageValidator = cafePageValidator;
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO createCampaign(UUID requesterUserId, CreateAdCampaignRequestDTO request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (payment.getBuyer() == null || !requesterUserId.equals(payment.getBuyer().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment belongs to another user");
        }
        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ad payment must be paid before creating campaign");
        }
        if (payment.getAdFee() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is not an ad fee payment");
        }
        if (adCampaignRepository.existsByPaymentPaymentId(payment.getPaymentId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment is already attached to an ad campaign");
        }

        CafePage cafePage = cafePageRepository.findById(request.getCafePageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe page not found"));
        if (cafePage.getOwner() == null || !requesterUserId.equals(cafePage.getOwner().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment buyer must be the cafe page owner");
        }

        AdCampaign campaign = new AdCampaign();
        campaign.setPayment(payment);
        campaign.setCafePage(cafePage);
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setTargetUrl(request.getTargetUrl());
        campaign.setPriority(1);
        campaign.setStatus(AdStatus.DRAFT);
        for (AdTargetRegionRequestDTO regionRequest : request.getTargetRegions() == null
                ? Collections.<AdTargetRegionRequestDTO>emptyList()
                : request.getTargetRegions()) {
            AdTargetRegion targetRegion = new AdTargetRegion();
            targetRegion.setAdCampaign(campaign);
            targetRegion.setProvince(regionRequest.getProvince());
            targetRegion.setCity(regionRequest.getCity());
            targetRegion.setArea(regionRequest.getArea());
            targetRegion.setWard(regionRequest.getWard());
            campaign.getTargetRegions().add(targetRegion);
        }
        if (request.isActivateNow()) {
            activate(campaign);
        }
        return adCampaignMapper.toResponse(adCampaignRepository.save(campaign));
    }

    @Override
    @Transactional(readOnly = true)
    public AdCampaignResponseDTO getCampaign(UUID requesterUserId, UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        validateCanManageCampaign(requesterUserId, campaign);
        return adCampaignMapper.toResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdCampaignResponseDTO> getCampaignsByCafePage(UUID requesterUserId, UUID cafePageId) {
        cafePageValidator.validateUserCanManagePage(cafePageId, requesterUserId);
        return adCampaignRepository.findByCafePageIdOrderByCreatedAtDesc(cafePageId)
                .stream()
                .map(adCampaignMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO pauseCampaign(UUID requesterUserId, UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        validateCanManageCampaign(requesterUserId, campaign);
        if (campaign.getStatus() != AdStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active campaigns can be paused");
        }
        campaign.setStatus(AdStatus.PAUSED);
        return adCampaignMapper.toResponse(adCampaignRepository.save(campaign));
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO activateCampaign(UUID requesterUserId, UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        validateCanManageCampaign(requesterUserId, campaign);
        activate(campaign);
        return adCampaignMapper.toResponse(adCampaignRepository.save(campaign));
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO recordClick(UUID requesterUserId, UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AdClick click = new AdClick();
        click.setAdCampaign(campaign);
        click.setUser(user);
        click.setClickedAt(LocalDateTime.now());
        adClickRepository.save(click);
        incrementDailyClicks(campaign, LocalDate.now());
        return adCampaignMapper.toResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public AdCampaignStatsResponseDTO getCampaignStats(UUID requesterUserId, UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        validateCanManageCampaign(requesterUserId, campaign);

        int servedImpressions = valueOrZero(campaign.getServedImpressions());
        int maxImpressions = valueOrZero(campaign.getMaxImpressions());
        long totalClicks = adClickRepository.countByAdCampaignAdCampaignId(adCampaignId);

        AdCampaignStatsResponseDTO response = new AdCampaignStatsResponseDTO();
        response.setCampaignId(adCampaignId);
        response.setServedImpressions(servedImpressions);
        response.setMaxImpressions(maxImpressions);
        response.setRemainingImpressions(Math.max(0, maxImpressions - servedImpressions));
        response.setTotalClicks(totalClicks);
        response.setCtrPercent(calculateCtrPercent(totalClicks, servedImpressions));
        response.setStartAt(campaign.getStartAt());
        response.setEndAt(campaign.getEndAt());
        response.setRemainingDays(calculateRemainingDays(campaign));
        response.setDailyStats(adDailyStatRepository
                .findByAdCampaignAdCampaignIdOrderByStatDateAsc(adCampaignId)
                .stream()
                .map(this::toDailyStatResponse)
                .toList());
        return response;
    }

    @Override
    @Transactional
    public void expireActiveCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        adCampaignRepository.findByStatus(AdStatus.ACTIVE).stream()
                .filter(campaign -> isExpired(campaign, now))
                .forEach(campaign -> {
                    campaign.setStatus(AdStatus.EXPIRED);
                    adCampaignRepository.save(campaign);
                });
    }

    private void activate(AdCampaign campaign) {
        if (campaign.getStatus() == AdStatus.REJECTED || campaign.getStatus() == AdStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejected or expired campaign cannot be activated");
        }
        LocalDateTime startAt = campaign.getStartAt() == null ? LocalDateTime.now() : campaign.getStartAt();
        campaign.setStartAt(startAt);
        if (campaign.getEndAt() == null) {
            campaign.setEndAt(startAt.plusDays(AdCampaign.DEFAULT_MAX_DURATION_DAYS));
        }
        campaign.setStatus(AdStatus.ACTIVE);
    }

    private boolean isExpired(AdCampaign campaign, LocalDateTime now) {
        if (campaign.getServedImpressions() >= campaign.getMaxImpressions()) {
            return true;
        }
        if (campaign.getStartAt() != null && !now.isBefore(campaign.getStartAt().plusDays(campaign.getMaxDurationDays()))) {
            return true;
        }
        return campaign.getEndAt() != null && now.isAfter(campaign.getEndAt());
    }

    private AdCampaign validateCampaignExists(UUID adCampaignId) {
        return adCampaignRepository.findById(adCampaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad campaign not found"));
    }

    private void validateCanManageCampaign(UUID requesterUserId, AdCampaign campaign) {
        cafePageValidator.validateUserCanManagePage(campaign.getCafePage().getId(), requesterUserId);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal calculateCtrPercent(long clicks, int impressions) {
        if (impressions <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(clicks)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(impressions), 2, RoundingMode.HALF_UP);
    }

    private long calculateRemainingDays(AdCampaign campaign) {
        if (campaign.getEndAt() == null) {
            return Math.max(0, valueOrZero(campaign.getMaxDurationDays()));
        }
        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), campaign.getEndAt().toLocalDate()));
    }

    private AdDailyStatResponseDTO toDailyStatResponse(AdDailyStat stat) {
        AdDailyStatResponseDTO response = new AdDailyStatResponseDTO();
        response.setStatDate(stat.getStatDate());
        response.setImpressions(valueOrZero(stat.getImpressions()));
        response.setClicks(valueOrZero(stat.getClicks()));
        return response;
    }

    private void incrementDailyClicks(AdCampaign campaign, LocalDate statDate) {
        AdDailyStat stat = adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(
                        campaign.getAdCampaignId(),
                        statDate)
                .orElseGet(() -> {
                    AdDailyStat newStat = new AdDailyStat();
                    newStat.setAdCampaign(campaign);
                    newStat.setStatDate(statDate);
                    return newStat;
                });
        stat.setClicks(stat.getClicks() + 1);
        adDailyStatRepository.save(stat);
    }
}
