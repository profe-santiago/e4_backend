ALTER TABLE payments RENAME COLUMN payment_method TO payment_intent_id;
ALTER TABLE payments ALTER COLUMN payment_intent_id TYPE VARCHAR(255);
