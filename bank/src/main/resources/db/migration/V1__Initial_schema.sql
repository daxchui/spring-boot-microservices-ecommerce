-- Create account table
CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER NOT NULL DEFAULT 0,
    owner_name VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'AUD',
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transfer table
CREATE TABLE transfer (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(255) UNIQUE,
    idempotency_key VARCHAR(255) UNIQUE,
    from_account_id BIGINT REFERENCES account(id),
    to_account_id BIGINT REFERENCES account(id),
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(20) NOT NULL DEFAULT 'CHARGE',
    failure_reason TEXT,
    order_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Create indexes for transfer table
CREATE INDEX idx_idempotency_key ON transfer(idempotency_key);
CREATE INDEX idx_correlation_id ON transfer(correlation_id);
CREATE INDEX idx_order_id ON transfer(order_id);

-- Create ledger_entry table
CREATE TABLE ledger_entry (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    delta NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for ledger_entry table
CREATE INDEX idx_transfer_id ON ledger_entry(transfer_id);
CREATE INDEX idx_account_id ON ledger_entry(account_id);

-- Create outbox_event table
CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- Create index for outbox_event table
CREATE INDEX idx_processed ON outbox_event(processed_at);
