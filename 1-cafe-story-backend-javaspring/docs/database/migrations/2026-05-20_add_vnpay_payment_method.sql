ALTER TABLE payments
    DROP CONSTRAINT IF EXISTS payments_payment_method_check;

ALTER TABLE payments
    ADD CONSTRAINT payments_payment_method_check
        CHECK (payment_method IN ('STRIPE_CARD', 'BANK_TRANSFER', 'VNPAY'));
