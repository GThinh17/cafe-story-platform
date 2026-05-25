# Payment API Endpoints

## Overview

The payment module lets a user purchase an active extra fee package. A payment is created for a buyer and an extra fee, then processed through one of the supported payment methods.

Supported flows:

- Stripe card payment through a Stripe Checkout Session.
- Bank transfer/manual transfer, where the backend generates a transfer content string and an operator marks the payment as paid after verification.
- VNPAY redirect payment, where VNPAY IPN confirms the payment status.

When a payment becomes successful, the service activates the purchased product. `REVIEWER_REGISTRATION` packages activate or renew the buyer's reviewer subscription by:

- creating or updating the buyer's `Reviewer` record;
- setting `reviewerActive` to `true`;
- setting `reviewerExpiresAt` to `now + extraFee.durationMonths`;
- assigning the `REVIEWER` role if the user does not already have it.

`CAFE_PAGE_OPENING` packages activate the buyer's cafe page package without requiring cafe page data in the payment request. On successful payment, the backend creates a draft `CafePage` for the buyer if none exists, ensures the buyer has an active `OWNER` `PageMember`, and assigns the `CAFE_PAGE` role if missing.

##

Run these service before payment
stripe listen --forward-to localhost:8080/api/payments/stripe/webhook
ngrok http 8080

## Base URL

```text
/api/payments
```

## Response Format

`PaymentController` returns `PaymentResponseDTO` directly, but the application-wide `GlobalResponseAdvice` wraps controller responses in `FormatResponse`.

Successful responses are therefore returned as:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "33333333-3333-3333-3333-333333333333",
    "buyerId": "11111111-1111-1111-1111-111111111111",
    "extraFeeId": "22222222-2222-2222-2222-222222222222",
    "paymentMethod": "STRIPE_CARD",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": "https://checkout.stripe.com/...",
    "qrCodeUrl": null,
    "transferContent": null,
    "createdAt": "2026-05-19T10:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-19T10:30:00"
  }
}
```

Error responses use the same wrapper with `status` set to `Fail` and `data` set to `null`.

## Data Model Summary

### CreatePaymentRequestDTO

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `buyerId` | UUID | Yes | ID of the user purchasing the extra fee package. |
| `extraFeeId` | UUID | Yes | ID of the extra fee package being purchased. The extra fee must exist and be active. |
| `paymentMethod` | `PaymentMethod` | Yes | Payment method. Supported values: `STRIPE_CARD`, `BANK_TRANSFER`, `VNPAY`. |

For `CAFE_PAGE_OPENING`, do not send cafe page `name`, `address`, or other page fields in this request.

### PaymentResponseDTO

| Field | Type | Description |
| --- | --- | --- |
| `paymentId` | UUID | Payment ID. |
| `buyerId` | UUID | Buyer user ID. |
| `extraFeeId` | UUID | Purchased extra fee package ID. |
| `paymentMethod` | `PaymentMethod` | `STRIPE_CARD`, `BANK_TRANSFER`, or `VNPAY`. |
| `amount` | decimal | Payment amount copied from `extraFee.price`. |
| `currency` | string | Currency code. Current implementation sets `VND`. |
| `paymentStatus` | `PaymentStatus` | Current payment status. New payments start as `PENDING`. |
| `paymentUrl` | string/null | Stripe Checkout URL for card payments. |
| `qrCodeUrl` | string/null | Provider QR code URL if available. Current implementation does not set it. |
| `transferContent` | string/null | Bank transfer content for manual transfer payments. |
| `createdAt` | datetime/null | Payment creation timestamp. |
| `paidAt` | datetime/null | Timestamp when payment was marked `PAID`. |
| `expiredAt` | datetime/null | Payment expiration timestamp. New payments expire 30 minutes after creation. |

### Enums

`PaymentMethod`:

```text
STRIPE_CARD
BANK_TRANSFER
VNPAY
```

`PaymentStatus`:

```text
PENDING
PAID
FAILED
CANCELLED
EXPIRED
REFUNDED
```

Only `PENDING` and `PAID` transitions are currently implemented by the payment service.

`ExtraFeeType`:

```text
REVIEWER_REGISTRATION
CAFE_PAGE_OPENING
```

Both `REVIEWER_REGISTRATION` and `CAFE_PAGE_OPENING` have activation logic after successful payment.

## Endpoints

### Create Payment

Creates a payment for an active extra fee package.

```text
POST /api/payments
```

#### Request Body

```json
{
  "buyerId": "11111111-1111-1111-1111-111111111111",
  "extraFeeId": "22222222-2222-2222-2222-222222222222",
  "paymentMethod": "STRIPE_CARD"
}
```

#### Behavior

The service:

- loads the buyer by `buyerId`;
- loads the extra fee by `extraFeeId`;
- rejects inactive extra fees;
- creates a `payments` row with:
  - `amount` copied from `extraFee.price`;
  - `currency` set to `VND`;
  - `paymentStatus` set to `PENDING`;
  - `expiredAt` set to 30 minutes after creation;
- creates a `payment_details` row based on the selected payment method.

For `STRIPE_CARD`, the backend creates a Stripe Checkout Session and stores:

- `providerName`: `STRIPE`;
- `providerOrderId`: Stripe Checkout Session ID;
- `providerPaymentUrl`: Stripe Checkout URL;
- `rawResponse`: raw Stripe session JSON.

For `BANK_TRANSFER`, the backend stores:

- `providerName`: `BANK_TRANSFER`;
- `transferContent`: `CAFE_PAYMENT_{paymentId}`;
- `note`: `Manual bank transfer payment. Mark as paid after transfer is verified.`

#### Stripe Configuration

Stripe card payment requires:

```properties
stripe.secret-key=sk_test_... or sk_live_...
app.payment.success-url=http://... or https://...
app.payment.cancel-url=http://... or https://...
```

The success and cancel URLs receive the `paymentId` appended as a query parameter. For example:

```text
https://example.com/payment/success?paymentId={paymentId}
```

#### Success Response

Stripe card payment:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "33333333-3333-3333-3333-333333333333",
    "buyerId": "11111111-1111-1111-1111-111111111111",
    "extraFeeId": "22222222-2222-2222-2222-222222222222",
    "paymentMethod": "STRIPE_CARD",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": "https://checkout.stripe.com/...",
    "qrCodeUrl": null,
    "transferContent": null,
    "createdAt": "2026-05-19T10:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-19T10:30:00"
  }
}
```

Bank transfer payment:

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "33333333-3333-3333-3333-333333333333",
    "buyerId": "11111111-1111-1111-1111-111111111111",
    "extraFeeId": "22222222-2222-2222-2222-222222222222",
    "paymentMethod": "BANK_TRANSFER",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": null,
    "qrCodeUrl": null,
    "transferContent": "CAFE_PAYMENT_33333333-3333-3333-3333-333333333333",
    "createdAt": "2026-05-19T10:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-19T10:30:00"
  }
}
```

#### Error Cases

| HTTP Status | Condition | Message |
| --- | --- | --- |
| `400` | Request body is missing required fields | Field validation message from `CreatePaymentRequestDTO`. |
| `400` | Extra fee exists but `status` is not `true` | `Extra fee is inactive` |
| `400` | Payment method is unsupported by service logic | `Invalid payment method` |
| `400` | Stripe secret key is missing | `Stripe secret key is not configured` |
| `400` | Stripe secret key does not start with `sk_test_` or `sk_live_` | `Stripe secret key format is invalid` |
| `400` | Stripe success URL is blank | `Stripe success URL is not configured` |
| `400` | Stripe cancel URL is blank | `Stripe cancel URL is not configured` |
| `400` | Stripe redirect URL does not start with `http://` or `https://` | `Stripe redirect URL must start with http:// or https://` |
| `400` | Stripe amount is missing or not positive | `Stripe payment amount must be greater than 0` |
| `400` | Stripe currency is blank | `Stripe payment currency is not configured` |
| `404` | Buyer does not exist | `Buyer not found` |
| `404` | Extra fee does not exist | `Extra fee not found` |
| `502` | Stripe Checkout Session creation fails | `Stripe checkout session creation failed: {safe Stripe error}` |

### Get Payment

Gets a payment by ID.

```text
GET /api/payments/{paymentId}
```

#### Path Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `paymentId` | UUID | Yes | Payment ID. |

#### Behavior

The service loads the payment by ID and fetches the matching `PaymentDetail` by `payment.paymentId`. If a detail row exists, provider fields such as `paymentUrl`, `qrCodeUrl`, and `transferContent` are mapped into the response.

#### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "33333333-3333-3333-3333-333333333333",
    "buyerId": "11111111-1111-1111-1111-111111111111",
    "extraFeeId": "22222222-2222-2222-2222-222222222222",
    "paymentMethod": "BANK_TRANSFER",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PENDING",
    "paymentUrl": null,
    "qrCodeUrl": null,
    "transferContent": "CAFE_PAYMENT_33333333-3333-3333-3333-333333333333",
    "createdAt": "2026-05-19T10:00:00",
    "paidAt": null,
    "expiredAt": "2026-05-19T10:30:00"
  }
}
```

#### Error Cases

| HTTP Status | Condition | Message |
| --- | --- | --- |
| `400` | `paymentId` is not a valid UUID | `paymentId must be a valid UUID` |
| `404` | Payment does not exist | `Payment not found` |

### Get Payments

Gets all payments, optionally filtered by payment status.

```text
GET /api/payments
GET /api/payments?paymentStatus=PENDING
```

#### Query Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `paymentStatus` | `PaymentStatus` | No | Optional status filter. Supported values are the `PaymentStatus` enum values. |

#### Behavior

If `paymentStatus` is omitted, the service returns all payments ordered by `createdAt` descending. If `paymentStatus` is provided, only payments with that status are returned. Each row includes matching `PaymentDetail` provider fields when available.

### Mark Bank Transfer Paid

Marks a bank transfer payment as paid after manual verification.

```text
POST /api/payments/{paymentId}/bank-transfer/mark-paid
```

#### Path Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `paymentId` | UUID | Yes | Payment ID. |

#### Behavior

The service:

- verifies that the payment exists;
- rejects payments that were not created with `BANK_TRANSFER`;
- changes `paymentStatus` to `PAID` if it is not already paid;
- sets `paidAt` to the current time;
- activates the purchased product.

This operation is idempotent for already paid payments at the status transition level. If the payment is already `PAID`, the service returns without re-running product activation.

For `REVIEWER_REGISTRATION` packages, product activation creates or updates the `Reviewer` record and assigns the `REVIEWER` role if needed.

For `CAFE_PAGE_OPENING` packages, product activation is idempotent:

- if the buyer has no cafe page, it creates a `DRAFT` page with name `"{userFullName}'s Cafe Page"` or `"{userName}'s Cafe Page"`, address `"Pending update"`, zero counters, `maxMembers` from the extra fee or `2`, `pageActive=true`, and the buyer's region when available;
- if the buyer already has a cafe page, it reuses the existing page, updates `maxMembers` from the purchased package, and does not create a duplicate;
- if `extraFee.durationMonths` is set, it extends `pageExpiresAt` from the current future expiry or from now;
- it ensures the buyer has an active `OWNER` `PageMember`;
- it assigns the `CAFE_PAGE` role if missing.

#### Success Response

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": {
    "paymentId": "33333333-3333-3333-3333-333333333333",
    "buyerId": "11111111-1111-1111-1111-111111111111",
    "extraFeeId": "22222222-2222-2222-2222-222222222222",
    "paymentMethod": "BANK_TRANSFER",
    "amount": 299000,
    "currency": "VND",
    "paymentStatus": "PAID",
    "paymentUrl": null,
    "qrCodeUrl": null,
    "transferContent": "CAFE_PAYMENT_33333333-3333-3333-3333-333333333333",
    "createdAt": "2026-05-19T10:00:00",
    "paidAt": "2026-05-19T10:10:00",
    "expiredAt": "2026-05-19T10:30:00"
  }
}
```

#### Error Cases

| HTTP Status | Condition | Message |
| --- | --- | --- |
| `400` | `paymentId` is not a valid UUID | `paymentId must be a valid UUID` |
| `400` | Payment method is not `BANK_TRANSFER` | `Payment is not a bank transfer` |
| `400` | Paid package is `REVIEWER_REGISTRATION` but `extraFee.durationMonths` is missing | `Reviewer package durationMonths is required` |
| `404` | Payment does not exist | `Payment not found` |

### Stripe Webhook

Handles Stripe webhook events.

```text
POST /api/payments/stripe/webhook
```

#### Headers

| Header | Required | Description |
| --- | --- | --- |
| `Stripe-Signature` | Required only when `stripe.webhook-secret` is configured | Stripe webhook signature header. |

#### Request Body

The endpoint receives the raw Stripe webhook payload as a string.

Example payload used by the current service logic:

```json
{
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_123",
      "payment_intent": "pi_test_123"
    }
  }
}
```

#### Behavior

If `stripe.webhook-secret` is configured, the backend verifies the payload using Stripe's webhook signature validation. If `stripe.webhook-secret` is blank, signature validation is skipped.

The current implementation processes only:

```text
checkout.session.completed
```

Other event types are ignored and return successfully.

For `checkout.session.completed`, the service:

- reads the Stripe Checkout Session ID from `data.object.id`;
- finds `PaymentDetail` by `providerOrderId`;
- stores `data.object.payment_intent` as `providerTransactionId`;
- stores the raw webhook payload in `rawResponse`;
- marks the related payment as `PAID`;
- activates the purchased product if the payment was not already paid.

#### Success Response

The controller method returns `void`. With the global response wrapper, a successful call is expected to return a success wrapper with `data` set to `null`.

```json
{
  "statusCode": 200,
  "status": "Success",
  "message": "Request processed successfully",
  "data": null
}
```

#### Error Cases

| HTTP Status | Condition | Message |
| --- | --- | --- |
| `400` | `stripe.webhook-secret` is configured and signature verification fails | `Invalid Stripe signature` |
| `400` | Payload cannot be parsed as expected JSON | `Invalid Stripe webhook payload` |
| `400` | Paid package is `REVIEWER_REGISTRATION` but `extraFee.durationMonths` is missing | `Reviewer package durationMonths is required` |
| `404` | `PaymentDetail` cannot be found by Stripe Checkout Session ID | `Payment detail not found` |

## Payment Lifecycle

```text
POST /api/payments
  -> PaymentStatus.PENDING
  -> Stripe Checkout completion webhook or manual bank transfer verification
  -> PaymentStatus.PAID
  -> Purchased product activation
```

Current implementation notes:

- New payments expire after 30 minutes by setting `expiredAt`, but there is no endpoint or scheduled job in the inspected payment service that automatically changes status to `EXPIRED`.
- `FAILED`, `CANCELLED`, `EXPIRED`, and `REFUNDED` enum values exist, but no endpoint in `PaymentController` currently transitions payments to those statuses.
- Stripe webhook processing is idempotent for already paid payments because `markPaymentPaid` returns immediately when `paymentStatus` is already `PAID`.
- Bank transfer marking is manual and does not require provider transaction data.
