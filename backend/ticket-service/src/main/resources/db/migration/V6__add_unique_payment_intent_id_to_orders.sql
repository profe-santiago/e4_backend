-- Remove duplicate orders keeping only the most recently created one per payment_intent_id
-- Must delete in FK order: tickets -> order_items -> orders

DELETE FROM tickets
WHERE order_item_id IN (
    SELECT oi.id FROM order_items oi
    WHERE oi.order_id NOT IN (
        SELECT DISTINCT ON (payment_intent_id) id
        FROM orders
        ORDER BY payment_intent_id, created_at DESC
    )
);

DELETE FROM order_items
WHERE order_id NOT IN (
    SELECT DISTINCT ON (payment_intent_id) id
    FROM orders
    ORDER BY payment_intent_id, created_at DESC
);

DELETE FROM orders
WHERE id NOT IN (
    SELECT DISTINCT ON (payment_intent_id) id
    FROM orders
    ORDER BY payment_intent_id, created_at DESC
);

ALTER TABLE orders ADD CONSTRAINT uq_orders_payment_intent_id UNIQUE (payment_intent_id);
