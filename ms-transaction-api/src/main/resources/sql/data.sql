-- ACCOUNTS
INSERT INTO accounts (id, account_number, cbu, cuit, holder_name, holder_type, email, phone, bank_code, branch_code, default_currency, is_active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ACC0001', '2850590940090418135201', '20123456789', 'Juan Perez', 'INDIVIDUAL', 'juan.perez@test.com', '1150000001', '285', '0000000001', 'ARS', TRUE),
    ('22222222-2222-2222-2222-222222222222', 'ACC0002', '2850590940090418135202', '20987654321', 'Maria Gomez', 'INDIVIDUAL', 'maria.gomez@test.com', '1150000002', '285', '0000000002', 'ARS', TRUE),
    ('33333333-3333-3333-3333-333333333333', 'ACC0003', '2850590940090418135203', '30712345678', 'Tech Solutions SA', 'COMPANY', 'finance@techsolutions.com', '1150000003', '285', '0000000003', 'USD', TRUE),
    ('44444444-4444-4444-4444-444444444444', 'ACC0004', '2850590940090418135204', '30798765432', 'Servicios Globales SRL', 'COMPANY', 'admin@serviciosglobales.com', '1150000004', '285', '0000000004', 'ARS', TRUE),
    ('55555555-5555-5555-5555-555555555555', 'ACC0005', '2850590940090418135205', '20333444556', 'Lucia Fernandez', 'INDIVIDUAL', 'lucia.fernandez@test.com', '1150000005', '285', '0000000005', 'EUR', TRUE);

-- INGESTED FILES
INSERT INTO ingested_files (id, file_name, file_format, status, checksum, file_size_bytes, total_rows, success_rows, failed_rows, uploaded_by, uploaded_at, processed_at)
VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'transactions_20260101.csv', 'CSV', 'COMPLETED', 'chk000000000000000000000000000000000000000000000000000000000001', 10240, 5, 5, 0, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'transactions_20260102.json', 'JSON', 'COMPLETED', 'chk000000000000000000000000000000000000000000000000000000000002', 20480, 4, 3, 1, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'transactions_20260103.xml', 'XML', 'FAILED', 'chk000000000000000000000000000000000000000000000000000000000003', 5120, 3, 1, 2, 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- TRANSACTIONS
INSERT INTO transactions (id, external_ref, transaction_at, ingested_at, type, status, amount, currency, benefactor_id, beneficiary_id, description, file_id, created_by, flagged, flag_reason)
VALUES
    ('bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'EXT-0001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'TRANSFER', 'COMPLETED', 15000.00, 'ARS',
     '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'Transferencia entre cuentas personales',
     'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'system', FALSE, NULL),

    ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'EXT-0002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PAYMENT', 'COMPLETED', 250000.50, 'ARS',
     '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', 'Pago de servicios',
     'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'system', FALSE, NULL),

    ('bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'EXT-0003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CREDIT', 'PENDING', 3200.75, 'USD',
     '33333333-3333-3333-3333-333333333333', '55555555-5555-5555-5555-555555555555', 'Pago internacional',
     'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'system', TRUE, 'Monto inusual para cuenta destino'),

    ('bbbbbbb4-bbbb-bbbb-bbbb-bbbbbbbbbbb4', 'EXT-0004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'DEBIT', 'FAILED', 500.00, 'ARS',
     '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 'Débito fallido por fondos insuficientes',
     'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'system', TRUE, 'Fondos insuficientes'),

    ('bbbbbbb5-bbbb-bbbb-bbbb-bbbbbbbbbbb5', 'EXT-0005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'REFUND', 'COMPLETED', 999.99, 'EUR',
     '44444444-4444-4444-4444-444444444444', '55555555-5555-5555-5555-555555555555', 'Reembolso de compra',
     'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'system', FALSE, NULL);

-- TRANSACTION VALIDATION WARNINGS
INSERT INTO transaction_validation_warnings (id, transaction_id, warning_code, warning_message)
VALUES
    ('ccccccc1-cccc-cccc-cccc-ccccccccccc1', 'bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'HIGH_AMOUNT', 'La transacción supera el umbral esperado para esta cuenta'),
    ('ccccccc2-cccc-cccc-cccc-ccccccccccc2', 'bbbbbbb4-bbbb-bbbb-bbbb-bbbbbbbbbbb4', 'INSUFFICIENT_FUNDS', 'La cuenta origen no posee saldo suficiente');