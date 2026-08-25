package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReviewerConnectOnboardResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStripeAccountResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerStripeAccount;
import com.cafestory.entity.User;
import com.cafestory.mapper.ReviewerStripeAccountMapper;
import com.cafestory.repository.ReviewerRepository;
import com.cafestory.repository.ReviewerStripeAccountRepository;
import com.cafestory.service.serviceImplement.ReviewerConnectServiceImpl;
import com.stripe.exception.ApiException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link ReviewerConnectServiceImpl} — onboarding Stripe Connect.
 *
 * <p>Mọi lời gọi Stripe ({@code Account.create}, {@code Account.retrieve},
 * {@code AccountLink.create}) đều là phương thức tĩnh nên được chặn bằng
 * {@code mockStatic}; không có yêu cầu mạng nào thoát ra ngoài.
 */
@ExtendWith(MockitoExtension.class)
class ReviewerConnectServiceImplTest {

    private static final String RETURN_URL = "http://localhost:3000/reviewer-dashboard/connect/return";
    private static final String REFRESH_URL = "http://localhost:3000/reviewer-dashboard/connect/refresh";

    @Mock
    private ReviewerRepository reviewerRepository;
    @Mock
    private ReviewerStripeAccountRepository stripeAccountRepository;

    private ReviewerConnectServiceImpl connectService;
    private Reviewer reviewer;

    @BeforeEach
    void setUp() {
        connectService = new ReviewerConnectServiceImpl(
                "sk_test_dummy",
                RETURN_URL,
                REFRESH_URL,
                reviewerRepository,
                stripeAccountRepository,
                new ReviewerStripeAccountMapper());

        reviewer = reviewer(true, LocalDateTime.now().plusMonths(1));
    }

    // ------------------------------------------------- createOnboardingLink

    @Test
    void createOnboardingLink_fail_reviewerNotFound_TC001() {
        UUID userId = UUID.randomUUID();
        when(reviewerRepository.findByUserUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.createOnboardingLink(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    @Test
    void createOnboardingLink_fail_subscriptionExpired_TC002() {
        Reviewer expired = reviewer(true, LocalDateTime.now().minusDays(1));
        when(reviewerRepository.findByUserUserId(expired.getUser().getUserId()))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> connectService.createOnboardingLink(expired.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer subscription is not active");
    }

    @Test
    void createOnboardingLink_fail_alreadyFullyOnboarded_TC003() {
        ReviewerStripeAccount account = stripeAccount("COMPLETE");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> connectService.createOnboardingLink(reviewer.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already fully onboarded");
    }

    @Test
    void createOnboardingLink_success_createsExpressAccountOnFirstCall_TC004() {
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        try (MockedStatic<Account> accounts = mockStatic(Account.class);
             MockedStatic<AccountLink> links = mockStatic(AccountLink.class)) {
            accounts.when(() -> Account.create(any(AccountCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(stripeApiAccount("acct_new", true, true));
            links.when(() -> AccountLink.create(any(AccountLinkCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(accountLink("https://connect.stripe.com/setup/e/acct_new"));

            ReviewerConnectOnboardResponseDTO result =
                    connectService.createOnboardingLink(reviewer.getUser().getUserId());

            assertThat(result.getStripeAccountId()).isEqualTo("acct_new");
            assertThat(result.getOnboardingUrl()).isEqualTo("https://connect.stripe.com/setup/e/acct_new");
            assertThat(result.getOnboardingStatus()).isEqualTo("PENDING");
        }

        ArgumentCaptor<ReviewerStripeAccount> captor = ArgumentCaptor.forClass(ReviewerStripeAccount.class);
        verify(stripeAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getStripeAccountId()).isEqualTo("acct_new");
        assertThat(captor.getValue().getReviewer()).isSameAs(reviewer);
    }

    @Test
    void createOnboardingLink_success_reusesIncompleteAccount_TC005() {
        ReviewerStripeAccount account = stripeAccount("INCOMPLETE");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<AccountLink> links = mockStatic(AccountLink.class)) {
            links.when(() -> AccountLink.create(any(AccountLinkCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(accountLink("https://connect.stripe.com/setup/e/acct_123"));

            ReviewerConnectOnboardResponseDTO result =
                    connectService.createOnboardingLink(reviewer.getUser().getUserId());

            assertThat(result.getStripeAccountId()).isEqualTo("acct_123");
            assertThat(result.getOnboardingStatus()).isEqualTo("INCOMPLETE");
        }

        verify(stripeAccountRepository, never()).save(any(ReviewerStripeAccount.class));
    }

    @Test
    void createOnboardingLink_fail_stripeAccountCreationFails_TC006() {
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        try (MockedStatic<Account> accounts = mockStatic(Account.class)) {
            accounts.when(() -> Account.create(any(AccountCreateParams.class), any(RequestOptions.class)))
                    .thenThrow(new ApiException("country not supported", "req_1", "code", 400, null));

            assertThatThrownBy(() -> connectService.createOnboardingLink(reviewer.getUser().getUserId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to create Stripe account")
                    .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                            .isEqualTo(HttpStatus.BAD_GATEWAY));
        }
    }

    @Test
    void createOnboardingLink_fail_accountLinkGenerationFails_TC007() {
        ReviewerStripeAccount account = stripeAccount("PENDING");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<AccountLink> links = mockStatic(AccountLink.class)) {
            links.when(() -> AccountLink.create(any(AccountLinkCreateParams.class), any(RequestOptions.class)))
                    .thenThrow(new ApiException("link expired", "req_2", "code", 400, null));

            assertThatThrownBy(() -> connectService.createOnboardingLink(reviewer.getUser().getUserId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to generate onboarding link");
        }
    }

    // ------------------------------------------------ refreshOnboardingLink

    @Test
    void refreshOnboardingLink_success_returnsFreshLink_TC008() {
        ReviewerStripeAccount account = stripeAccount("INCOMPLETE");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<AccountLink> links = mockStatic(AccountLink.class)) {
            links.when(() -> AccountLink.create(any(AccountLinkCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(accountLink("https://connect.stripe.com/setup/e/refresh"));

            ReviewerConnectOnboardResponseDTO result =
                    connectService.refreshOnboardingLink(reviewer.getUser().getUserId());

            assertThat(result.getOnboardingUrl()).isEqualTo("https://connect.stripe.com/setup/e/refresh");
            assertThat(result.getOnboardingStatus()).isEqualTo("INCOMPLETE");
        }
    }

    @Test
    void refreshOnboardingLink_fail_reviewerNotFound_TC009() {
        UUID userId = UUID.randomUUID();
        when(reviewerRepository.findByUserUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.refreshOnboardingLink(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    @Test
    void refreshOnboardingLink_fail_subscriptionInactive_TC010() {
        Reviewer inactive = reviewer(false, LocalDateTime.now().plusMonths(1));
        when(reviewerRepository.findByUserUserId(inactive.getUser().getUserId()))
                .thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> connectService.refreshOnboardingLink(inactive.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer subscription is not active");
    }

    @Test
    void refreshOnboardingLink_fail_noStripeAccountYet_TC011() {
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.refreshOnboardingLink(reviewer.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Start onboarding first");
    }

    // ------------------------------------------------- handleAccountUpdated

    @Test
    void handleAccountUpdated_success_marksCompleteWhenBothFlagsOn_TC012() {
        ReviewerStripeAccount account = stripeAccount("PENDING");
        when(stripeAccountRepository.findByStripeAccountId("acct_123")).thenReturn(Optional.of(account));

        connectService.handleAccountUpdated("acct_123", true, true);

        assertThat(account.getOnboardingStatus()).isEqualTo("COMPLETE");
        assertThat(account.isChargesEnabled()).isTrue();
        assertThat(account.isPayoutsEnabled()).isTrue();
        verify(stripeAccountRepository).save(account);
    }

    @Test
    void handleAccountUpdated_success_marksIncompleteWhenPayoutsOff_TC013() {
        ReviewerStripeAccount account = stripeAccount("COMPLETE");
        when(stripeAccountRepository.findByStripeAccountId("acct_123")).thenReturn(Optional.of(account));

        connectService.handleAccountUpdated("acct_123", true, false);

        assertThat(account.getOnboardingStatus()).isEqualTo("INCOMPLETE");
    }

    @Test
    void handleAccountUpdated_success_unknownAccountIsIgnored_TC014() {
        when(stripeAccountRepository.findByStripeAccountId("acct_unknown")).thenReturn(Optional.empty());

        connectService.handleAccountUpdated("acct_unknown", true, true);

        verify(stripeAccountRepository, never()).save(any(ReviewerStripeAccount.class));
    }

    // ------------------------------------------------------ getStatus / sync

    @Test
    void getStatus_success_mapsAccountToResponse_TC015() {
        ReviewerStripeAccount account = stripeAccount("COMPLETE");
        account.setChargesEnabled(true);
        account.setPayoutsEnabled(true);
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        ReviewerStripeAccountResponseDTO result = connectService.getStatus(reviewer.getUser().getUserId());

        assertThat(result.getReviewerId()).isEqualTo(reviewer.getReviewerId());
        assertThat(result.getUserName()).isEqualTo("an");
        assertThat(result.getStripeAccountId()).isEqualTo("acct_123");
        assertThat(result.getOnboardingStatus()).isEqualTo("COMPLETE");
        assertThat(result.isChargesEnabled()).isTrue();
        assertThat(result.isPayoutsEnabled()).isTrue();
    }

    @Test
    void getStatus_fail_reviewerNotFound_TC016() {
        UUID userId = UUID.randomUUID();
        when(reviewerRepository.findByUserUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.getStatus(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    @Test
    void getStatus_fail_noStripeAccount_TC017() {
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.getStatus(reviewer.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No Stripe account found");
    }

    @Test
    void sync_success_pullsFlagsFromStripe_TC018() {
        ReviewerStripeAccount account = stripeAccount("PENDING");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<Account> accounts = mockStatic(Account.class)) {
            accounts.when(() -> Account.retrieve(anyString(), any(RequestOptions.class)))
                    .thenReturn(stripeApiAccount("acct_123", true, true));

            ReviewerStripeAccountResponseDTO result = connectService.sync(reviewer.getUser().getUserId());

            assertThat(result.getOnboardingStatus()).isEqualTo("COMPLETE");
        }

        assertThat(account.isChargesEnabled()).isTrue();
        assertThat(account.isPayoutsEnabled()).isTrue();
        verify(stripeAccountRepository).save(account);
    }

    @Test
    void sync_success_partialVerificationStaysIncomplete_TC019() {
        ReviewerStripeAccount account = stripeAccount("PENDING");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<Account> accounts = mockStatic(Account.class)) {
            accounts.when(() -> Account.retrieve(anyString(), any(RequestOptions.class)))
                    .thenReturn(stripeApiAccount("acct_123", true, null));

            ReviewerStripeAccountResponseDTO result = connectService.sync(reviewer.getUser().getUserId());

            assertThat(result.getOnboardingStatus()).isEqualTo("INCOMPLETE");
            assertThat(result.isPayoutsEnabled()).isFalse();
        }
    }

    @Test
    void sync_fail_stripeRetrieveFails_TC020() {
        ReviewerStripeAccount account = stripeAccount("PENDING");
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.of(account));

        try (MockedStatic<Account> accounts = mockStatic(Account.class)) {
            accounts.when(() -> Account.retrieve(anyString(), any(RequestOptions.class)))
                    .thenThrow(new ApiException("no such account", "req_3", "code", 404, null));

            assertThatThrownBy(() -> connectService.sync(reviewer.getUser().getUserId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Failed to sync Stripe account");
        }

        verify(stripeAccountRepository, never()).save(any(ReviewerStripeAccount.class));
    }

    @Test
    void sync_fail_noStripeAccount_TC021() {
        when(reviewerRepository.findByUserUserId(reviewer.getUser().getUserId()))
                .thenReturn(Optional.of(reviewer));
        when(stripeAccountRepository.findByReviewerReviewerId(reviewer.getReviewerId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.sync(reviewer.getUser().getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Start onboarding first");
    }

    @Test
    void sync_fail_reviewerNotFound_TC022() {
        UUID userId = UUID.randomUUID();
        when(reviewerRepository.findByUserUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> connectService.sync(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Reviewer not found");
    }

    // ------------------------------------------------------------- Helpers

    private Reviewer reviewer(boolean active, LocalDateTime expiresAt) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("an");
        user.setUserAvatar("https://cdn.example.com/an.png");
        Reviewer newReviewer = new Reviewer();
        newReviewer.setReviewerId(UUID.randomUUID());
        newReviewer.setUser(user);
        newReviewer.setReviewerActive(active);
        newReviewer.setReviewerExpiresAt(expiresAt);
        return newReviewer;
    }

    private ReviewerStripeAccount stripeAccount(String onboardingStatus) {
        ReviewerStripeAccount account = new ReviewerStripeAccount();
        account.setId(UUID.randomUUID());
        account.setReviewer(reviewer);
        account.setStripeAccountId("acct_123");
        account.setOnboardingStatus(onboardingStatus);
        return account;
    }

    private Account stripeApiAccount(String id, Boolean chargesEnabled, Boolean payoutsEnabled) {
        Account account = new Account();
        account.setId(id);
        account.setChargesEnabled(chargesEnabled);
        account.setPayoutsEnabled(payoutsEnabled);
        return account;
    }

    private AccountLink accountLink(String url) {
        AccountLink link = new AccountLink();
        link.setUrl(url);
        return link;
    }
}
