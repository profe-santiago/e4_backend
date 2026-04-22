-- Índice para consultas paginadas por usuario (query más frecuente del servicio)
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);
