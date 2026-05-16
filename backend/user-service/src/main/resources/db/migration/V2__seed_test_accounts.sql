-- Perfiles de usuario para las cuentas de prueba.
-- Los UUIDs coinciden con los del seed del auth-service.

INSERT INTO users (id, first_name, last_name, email)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'Admin',  'Test', 'admin@test.com'),
  ('00000000-0000-0000-0000-000000000002', 'Buyer',  'Test', 'buyer@test.com')
ON CONFLICT (id) DO NOTHING;
