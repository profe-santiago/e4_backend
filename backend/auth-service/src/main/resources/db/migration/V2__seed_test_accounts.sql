-- Cuentas de prueba. Contraseñas: Admin123! y Buyer123!
-- Los UUIDs son fijos para que coincidan con el seed del user-service.

INSERT INTO credentials (user_id, email, password_hash, role, is_active)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@test.com', '$2a$10$.OPOGpfUuzJ7nTFPO43VQuY3D72mbd2dVIcd5HeLoS.t4kE2CbGpa', 'ADMIN', true),
  ('00000000-0000-0000-0000-000000000002', 'buyer@test.com', '$2a$10$n6jflDmmCAMG1kDY5zZ07.Gtu2Rtaq.SlHd5Uxz1UYU6.KjcyyyrW', 'BUYER', true)
ON CONFLICT (email) DO NOTHING;
