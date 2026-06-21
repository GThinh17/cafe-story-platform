package com.cafestory.controller;

import com.cafestory.service.serviceInterface.ReviewerConnectService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stripe-connect")
public class StripeConnectWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeConnectWebhookController.class);

    private final String connectWebhookSecret;
    private final ReviewerConnectService connectService;

    public StripeConnectWebhookController(
            @Value("${stripe.connect-webhook-secret:}") String connectWebhookSecret,
            ReviewerConnectService connectService) {
        this.connectWebhookSecret = connectWebhookSecret;
        this.connectService = connectService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, connectWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe Connect webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "account.updated" -> {
                event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                    Account account = (Account) obj;
                    connectService.handleAccountUpdated(
                            account.getId(),
                            Boolean.TRUE.equals(account.getChargesEnabled()),
                            Boolean.TRUE.equals(account.getPayoutsEnabled())
                    );
                });
            }
            case "transfer.failed" ->
                log.error("Stripe transfer failed — event id: {}", event.getId());
            case "account.application.deauthorized" ->
                log.warn("Reviewer deauthorized Stripe Connect — event id: {}", event.getId());
            default ->
                log.debug("Unhandled Stripe Connect event: {}", event.getType());
        }

        return ResponseEntity.ok("ok");
    }
}
