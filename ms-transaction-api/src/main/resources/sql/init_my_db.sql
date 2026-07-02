-- Tabla de cuentas
CREATE TABLE IF NOT EXISTS accounts (
                                        id UUID PRIMARY KEY,
                                        account_number VARCHAR(50) UNIQUE,
    cbu VARCHAR(22) UNIQUE,
    cuit VARCHAR(11) UNIQUE,
    holder_name VARCHAR(255),
    holder_type VARCHAR(20) CHECK (holder_type IN ('INDIVIDUAL','COMPANY')),
    email VARCHAR(255),
    phone VARCHAR(50),
    bank_code VARCHAR(20),
    branch_code CHAR(10),
    default_currency CHAR(3), -- ISO 4217
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP ,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );


-- Tabla de archivos ingeridos
CREATE TABLE IF NOT EXISTS ingested_files (  id UUID PRIMARY KEY,
                                              file_name VARCHAR(255),
    file_format VARCHAR(10) CHECK (file_format IN ('CSV','JSON','XML')),
    status VARCHAR(20) CHECK (status IN ('RECEIVED','PROCESSING','COMPLETED','FAILED','DUPLICATE')),
    checksum VARCHAR(64) UNIQUE, -- SHA-256
    file_size_bytes BIGINT,
    total_rows INT,
    success_rows INT,
    failed_rows INT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP ,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                              );

-- Tabla de transacciones
CREATE TABLE IF NOT EXISTS transactions (
                                            id UUID PRIMARY KEY,
                                            external_ref VARCHAR(100) UNIQUE,
    transaction_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ingested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(20) CHECK (type IN ('DEBIT','CREDIT','TRANSFER','PAYMENT','REFUND','FEE')),
    status VARCHAR(20) CHECK (status IN ('PENDING','COMPLETED','FAILED','REVERSED')),
    amount NUMERIC(18,2),
    currency CHAR(3), -- ISO 4217
    benefactor_id UUID REFERENCES accounts(id),
    beneficiary_id UUID REFERENCES accounts(id),
    description TEXT,
    file_id UUID REFERENCES ingested_files(id),
    created_by VARCHAR(100),
    flagged BOOLEAN DEFAULT FALSE,
    flag_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );


-- Tabla de advertencias de validación
CREATE TABLE IF NOT EXISTS transaction_validation_warnings (
                                                               id UUID PRIMARY KEY,
                                                               transaction_id UUID REFERENCES transactions(id),
    warning_code VARCHAR(50),
    warning_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );