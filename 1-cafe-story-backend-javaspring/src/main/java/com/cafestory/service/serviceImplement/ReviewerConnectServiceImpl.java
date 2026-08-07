package com.cafestory.service.serviceImplement;

import com.cafestory.dto.responseDTO.ReviewerConnectOnboardResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStripeAccountResponseDTO;
import com.cafestory.mapper.ReviewerStripeAccountMapper;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerStripeAccount;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.ReviewerStripeAccountRepository;
import com.cafestory.service.serviceInterface.ReviewerConnectService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountLinkCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewerConnectServiceImpl implements ReviewerConnectService {

    private static final Logger log = LoggerFactory.getLogger(ReviewerConnectServiceImpl.class);

    private final String secretKey;
    private final String returnUrl;
    private final String refreshUrl;
    private final ReviewerRepository reviewerRepository;
    private final ReviewerStripeAccountRepository stripeAccountRepository;
    private final ReviewerStripeAccountMapper mapper;

    public ReviewerConnectServiceImpl(
            @Value("${stripe.secret-key:}") String secretKey,
            // Default phải khớp application.properties — trước đây lệch
            // (/reviewer/... thay vì /reviewer-dashboard/...) nên chạy thiếu file
            // config là Stripe trả người dùng về route không tồn tại.
            @Value("${stripe.connect.return-url:http://localhost:3000/reviewer-dashboard/connect/return}") String returnUrl,
            @Value("${stripe.connect.refresh-url:http://localhost:3000/reviewer-dashboard/connect/refresh}") String refreshUrl,
            ReviewerRepository reviewerRepository,
            ReviewerStripeAccountRepository stripeAccountRepository,
            ReviewerStripeAccountMapper mapper) {
        this.secretKey = secretKey;
        this.returnUrl = returnUrl;
        this.refreshUrl = refreshUrl;
        this.reviewerRepository = reviewerRepository;
        this.stripeAccountRepository = stripeAccountRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReviewerConnectOnboardResponseDTO createOnboardingLink(UUID userId) {
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));

        if (!reviewer.isSubscriptionActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reviewer subscription is not active");
        }

        Optional<ReviewerStripeAccount> existing =
                stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId());

        if (existing.isPresent() && "COMPLETE".equals(existing.get().getOnboardingStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe account already fully onboarded");
        }

        String stripeAccountId;
        if (existing.isPresent()) {
            stripeAccountId = existing.get().getStripeAccountId();
        } else {
            stripeAccountId = createExpressAccount();
            ReviewerStripeAccount account = new ReviewerStripeAccount();
            account.setReviewer(reviewer);
            account.setStripeAccountId(stripeAccountId);
            stripeAccountRepository.save(account);
        }

        String onboardingUrl = generateAccountLink(stripeAccountId);

        ReviewerConnectOnboardResponseDTO dto = new ReviewerConnectOnboardResponseDTO();
        dto.setOnboardingUrl(onboardingUrl);
        dto.setStripeAccountId(stripeAccountId);
        dto.setOnboardingStatus(existing.map(ReviewerStripeAccount::getOnboardingStatus).orElse("PENDING"));
        return dto;
    }

    @Override
    @Transactional
    public ReviewerConnectOnboardResponseDTO refreshOnboardingLink(UUID userId) {
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));

        if (!reviewer.isSubscriptionActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reviewer subscription is not active");
        }

        ReviewerStripeAccount stripeAccount = stripeAccountRepository
                .findByReviewerReviewerId(reviewer.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No Stripe account found. Start onboarding first."));

        String onboardingUrl = generateAccountLink(stripeAccount.getStripeAccountId());

        ReviewerConnectOnboardResponseDTO dto = new ReviewerConnectOnboardResponseDTO();
        dto.setOnboardingUrl(onboardingUrl);
        dto.setStripeAccountId(stripeAccount.getStripeAccountId());
        dto.setOnboardingStatus(stripeAccount.getOnboardingStatus());
        return dto;
    }

    @Override
    @Transactional
    public void handleAccountUpdated(String stripeAccountId, boolean chargesEnabled, boolean payoutsEnabled) {
        stripeAccountRepository.findByStripeAccountId(stripeAccountId).ifPresent(account -> {
            account.setChargesEnabled(chargesEnabled);
            account.setPayoutsEnabled(payoutsEnabled);
            account.setOnboardingStatus(chargesEnabled && payoutsEnabled ? "COMPLETE" : "INCOMPLETE");
            stripeAccountRepository.save(account);
            log.info("Stripe account {} updated: chargesEnabled={}, payoutsEnabled={}",
                    stripeAccountId, chargesEnabled, payoutsEnabled);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewerStripeAccountResponseDTO getStatus(UUID userId) {
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));

        ReviewerStripeAccount account = stripeAccountRepository
                .findByReviewerReviewerId(reviewer.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No Stripe account found"));

        return mapper.toResponse(reviewer, account);
    }

    @Override
    @Transactional
    public ReviewerStripeAccountResponseDTO sync(UUID userId) {
        Reviewer reviewer = reviewerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reviewer not found"));

        ReviewerStripeAccount account = stripeAccountRepository
                .findByReviewerReviewerId(reviewer.getReviewerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No Stripe account found. Start onboarding first."));

        try {
            Account stripeAccount = Account.retrieve(account.getStripeAccountId(), requestOptions());
            boolean chargesEnabled = Boolean.TRUE.equals(stripeAccount.getChargesEnabled());
            boolean payoutsEnabled = Boolean.TRUE.equals(stripeAccount.getPayoutsEnabled());
            account.setChargesEnabled(chargesEnabled);
            account.setPayoutsEnabled(payoutsEnabled);
            account.setOnboardingStatus(chargesEnabled && payoutsEnabled ? "COMPLETE" : "INCOMPLETE");
            stripeAccountRepository.save(account);
            log.info("Synced Stripe account {}: chargesEnabled={}, payoutsEnabled={}",
                    account.getStripeAccountId(), chargesEnabled, payoutsEnabled);
        } catch (StripeException e) {
            log.error("Failed to sync Stripe account {}: {}", account.getStripeAccountId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to sync Stripe account: " + e.getMessage());
        }

        return mapper.toResponse(reviewer, account);
    }

    /**
     * Truyền API key theo từng lời gọi thay vì gán Stripe.apiKey — biến static
     * toàn cục đó bị bốn class cùng ghi, đổi key ở đâu là ảnh hưởng cả tiến trình.
     */
    private RequestOptions requestOptions() {
        return RequestOptions.builder().setApiKey(secretKey).build();
    }

    private String createExpressAccount() {
        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("VN")
                    .setTosAcceptance(
                            AccountCreateParams.TosAcceptance.builder()
                                    .setServiceAgreement("full")
                                    .build()
                    )
                    .build();
            Account account = Account.create(params, requestOptions());
            return account.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe Express account: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to create Stripe account: " + e.getMessage());
        }
    }

    private String generateAccountLink(String stripeAccountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setRefreshUrl(refreshUrl)
                    .setReturnUrl(returnUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();
            AccountLink link = AccountLink.create(params, requestOptions());
            return link.getUrl();
        } catch (StripeException e) {
            log.error("Failed to generate Stripe account link for {}: {}", stripeAccountId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Failed to generate onboarding link: " + e.getMessage());
        }
    }
}
