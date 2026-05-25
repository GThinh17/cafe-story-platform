package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdTargetRegionRequestDTO;
import com.cafestory.dto.requestDTO.CreateAdCampaignRequestDTO;
import com.cafestory.dto.requestDTO.RecordAdClickRequestDTO;
import com.cafestory.dto.responseDTO.AdCampaignResponseDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public AdCampaignServiceImpl(
            AdCampaignRepository adCampaignRepository,
            AdClickRepository adClickRepository,
            AdDailyStatRepository adDailyStatRepository,
            CafePageRepository cafePageRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            AdCampaignMapper adCampaignMapper) {
        this.adCampaignRepository = adCampaignRepository;
        this.adClickRepository = adClickRepository;
        this.adDailyStatRepository = adDailyStatRepository;
        this.cafePageRepository = cafePageRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.adCampaignMapper = adCampaignMapper;
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO createCampaign(CreateAdCampaignRequestDTO request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
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
        if (cafePage.getOwner() == null || payment.getBuyer() == null
                || !cafePage.getOwner().getUserId().equals(payment.getBuyer().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment buyer must own the cafe page");
        }

        AdCampaign campaign = new AdCampaign();
        campaign.setPayment(payment);
        campaign.setCafePage(cafePage);
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setTargetUrl(request.getTargetUrl());
        campaign.setPriority(request.getPriority() == null ? 1 : request.getPriority());
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
    public AdCampaignResponseDTO getCampaign(UUID adCampaignId) {
        return adCampaignMapper.toResponse(validateCampaignExists(adCampaignId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdCampaignResponseDTO> getCampaignsByCafePage(UUID cafePageId) {
        return adCampaignRepository.findByCafePageIdOrderByCreatedAtDesc(cafePageId)
                .stream()
                .map(adCampaignMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO pauseCampaign(UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        if (campaign.getStatus() != AdStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active campaigns can be paused");
        }
        campaign.setStatus(AdStatus.PAUSED);
        return adCampaignMapper.toResponse(adCampaignRepository.save(campaign));
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO activateCampaign(UUID adCampaignId) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        activate(campaign);
        return adCampaignMapper.toResponse(adCampaignRepository.save(campaign));
    }

    @Override
    @Transactional
    public AdCampaignResponseDTO recordClick(UUID adCampaignId, RecordAdClickRequestDTO request) {
        AdCampaign campaign = validateCampaignExists(adCampaignId);
        User user = request.getUserId() == null ? null : userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        AdClick click = new AdClick();
        click.setAdCampaign(campaign);
        click.setUser(user);
        click.setClickedAt(LocalDateTime.now());
        adClickRepository.save(click);
        incrementDailyStat(campaign, LocalDate.now(), false);
        return adCampaignMapper.toResponse(campaign);
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

    private void incrementDailyStat(AdCampaign campaign, LocalDate statDate, boolean impression) {
        AdDailyStat stat = adDailyStatRepository.findByAdCampaignAdCampaignIdAndStatDate(
                        campaign.getAdCampaignId(),
                        statDate)
                .orElseGet(() -> {
                    AdDailyStat newStat = new AdDailyStat();
                    newStat.setAdCampaign(campaign);
                    newStat.setStatDate(statDate);
                    return newStat;
                });
        if (impression) {
            stat.setImpressions(stat.getImpressions() + 1);
        } else {
            stat.setClicks(stat.getClicks() + 1);
        }
        adDailyStatRepository.save(stat);
    }
}
