-- Almacena el ID del método de pago de Stripe (ej: pm_xxxxx)
-- enviado por el cliente al crear la orden para procesarlo al confirmar el stock.
ALTER TABLE orders ADD COLUMN payment_method_id VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE orders ALTER COLUMN payment_method_id DROP DEFAULT;
