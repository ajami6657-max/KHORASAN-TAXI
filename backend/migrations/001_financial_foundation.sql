-- Zaffran Taxi financial foundation (PostgreSQL)
-- Amounts are integer IRR (rials). Never use floating point for money.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS driver_financial_profiles (
    driver_id UUID PRIMARY KEY,
    preferred_payout_mode TEXT NOT NULL DEFAULT 'daily'
        CHECK (preferred_payout_mode IN ('instant','daily')),
    verified_iban TEXT,
    iban_verified_at TIMESTAMPTZ,
    payouts_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type TEXT NOT NULL CHECK (owner_type IN ('platform','driver','gateway','clearing')),
    owner_id UUID,
    currency CHAR(3) NOT NULL DEFAULT 'IRR',
    account_code TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(owner_type, owner_id, currency, account_code)
);

CREATE TABLE IF NOT EXISTS ledger_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_type TEXT NOT NULL,
    reference_id TEXT NOT NULL,
    idempotency_key TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(reference_type, reference_id)
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES ledger_transactions(id),
    account_id UUID NOT NULL REFERENCES ledger_accounts(id),
    debit_irr BIGINT NOT NULL DEFAULT 0 CHECK (debit_irr >= 0),
    credit_irr BIGINT NOT NULL DEFAULT 0 CHECK (credit_irr >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK ((debit_irr > 0 AND credit_irr = 0) OR (credit_irr > 0 AND debit_irr = 0))
);
CREATE INDEX IF NOT EXISTS idx_ledger_entries_account ON ledger_entries(account_id, created_at);

CREATE TABLE IF NOT EXISTS driver_payouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES driver_financial_profiles(driver_id),
    mode TEXT NOT NULL CHECK (mode IN ('instant','daily')),
    amount_irr BIGINT NOT NULL CHECK (amount_irr > 0),
    fee_irr BIGINT NOT NULL DEFAULT 0 CHECK (fee_irr >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'IRR',
    idempotency_key TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL DEFAULT 'requested'
        CHECK (status IN ('requested','processing','paid','rejected','failed','returned','cancelled')),
    provider TEXT,
    provider_reference TEXT,
    failure_reason TEXT,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    settled_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_driver_payouts_daily
    ON driver_payouts(status, requested_at) WHERE mode = 'daily';
CREATE INDEX IF NOT EXISTS idx_driver_payouts_driver
    ON driver_payouts(driver_id, requested_at DESC);

CREATE TABLE IF NOT EXISTS payout_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_date DATE NOT NULL,
    status TEXT NOT NULL DEFAULT 'created'
        CHECK (status IN ('created','processing','completed','partially_failed','failed')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(business_date)
);

CREATE TABLE IF NOT EXISTS payout_batch_items (
    batch_id UUID NOT NULL REFERENCES payout_batches(id),
    payout_id UUID NOT NULL UNIQUE REFERENCES driver_payouts(id),
    PRIMARY KEY(batch_id, payout_id)
);

-- Ledger balancing is validated by the application in the same DB transaction:
-- SUM(debit_irr) must equal SUM(credit_irr) for each ledger_transactions.id.
-- Posted entries must be immutable; corrections use compensating transactions.
