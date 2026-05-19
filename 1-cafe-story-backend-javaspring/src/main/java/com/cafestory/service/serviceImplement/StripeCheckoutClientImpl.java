package com.cafestory.service.serviceImplement;

import com.cafestory.entity.ExtraFee;
import com.cafestory.entity.Payment;
import com.cafestory.service.serviceInterface.StripeCheckoutClient;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class StripeCheckoutClientImpl implements StripeCheckoutClient {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutClientImpl.class);

    private final String secretKey;
    private final String successUrl;
    private final String cancelUrl;

    public StripeCheckoutClientImpl(
            @Value("${stripe.secret-key:}") String secretKey,
            @Value("${app.payment.success-url:}") String successUrl,
            @Value("${app.payment.cancel-url:}") String cancelUrl) {
        this.secretKey = secretKey;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    @Override
    public StripeCheckoutSession createCheckoutSession(Payment payment, ExtraFee extraFee) {
        validateStripeConfig(payment);
        try {
            Stripe.apiKey = secretKey;
            String currency = payment.getCurrency().toLowerCase();
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(urlWithPaymentId(successUrl, payment))
                    .setCancelUrl(urlWithPaymentId(cancelUrl, payment))
                    .putMetadata("paymentId", payment.getPaymentId().toString())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(payment.getAmount().longValueExact())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(extraFee.getName())
                                            .setDescription(extraFee.getDescription())
                                            .build())
                                    .build())
                            .build())
                    .build();
            Session session = Session.create(params);
            return new StripeCheckoutSession(session.getId(), session.getUrl(), session.toJson());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (StripeException ex) {
            log.error(
                    "Stripe checkout session creation failed. message={}, code={}, statusCode={}, requestId={}",
                    ex.getMessage(),
                    ex.getCode(),
                    ex.getStatusCode(),
                    ex.getRequestId(),
                    ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Stripe checkout session creation failed: " + safeStripeMessage(ex));
        } catch (Exception ex) {
            log.error("Stripe checkout session creation failed. message={}", ex.getMessage(), ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Stripe checkout session creation failed: " + safeMessage(ex.getMessage()));
        }
    }

    private void validateStripeConfig(Payment payment) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe secret key is not configured");
        }
        if (!secretKey.startsWith("sk_test_") && !secretKey.startsWith("sk_live_")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe secret key format is invalid");
        }
        validateUrl(successUrl, "Stripe success URL is not configured");
        validateUrl(cancelUrl, "Stripe cancel URL is not configured");
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe payment amount must be greater than 0");
        }
        if (payment.getCurrency() == null || payment.getCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe payment currency is not configured");
        }
    }

    private void validateUrl(String url, String blankMessage) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, blankMessage);
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe redirect URL must start with http:// or https://");
        }
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "unknown Stripe error" : message;
    }

    private String safeStripeMessage(StripeException ex) {
        String message = ex.getUserMessage();
        if (message == null || message.isBlank()) {
            message = ex.getMessage();
        }
        if (message == null || message.isBlank()) {
            return "unknown Stripe error";
        }
        int metadataStart = message.indexOf("; code:");
        return metadataStart >= 0 ? message.substring(0, metadataStart) : message;
    }

    private String urlWithPaymentId(String baseUrl, Payment payment) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "paymentId=" + payment.getPaymentId();
    }
}
