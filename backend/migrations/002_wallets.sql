-- Zaffran Taxi internal wallet layer.
-- Wallet balances are derived from the double-entry ledger; no writable balance column.
ALTER TABLE ledger_accounts DROP CONSTRAINT IF EXISTS ledger_accounts_owner_type_check;
ALTER TABLE ledger_accounts ADD CONSTRAINT ledger_accounts_owner_type_check
 CHECK (owner_type IN ('platform','driver','passenger','gateway','clearing'));

CREATE TABLE IF NOT EXISTS wallets (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 owner_type TEXT NOT NULL CHECK (owner_type IN ('passenger','driver')),
 owner_id UUID NOT NULL,
 ledger_account_id UUID NOT NULL UNIQUE REFERENCES ledger_accounts(id),
 currency CHAR(3) NOT NULL DEFAULT 'IRR',
 status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active','restricted','closed')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(owner_type, owner_id, currency)
);

CREATE TABLE IF NOT EXISTS wallet_operations (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 wallet_id UUID NOT NULL REFERENCES wallets(id),
 operation_type TEXT NOT NULL CHECK (operation_type IN
  ('top_up','trip_payment','trip_refund','promotion_credit','driver_earning',
   'payout_reservation','payout_release','manual_adjustment')),
 amount_irr BIGINT NOT NULL CHECK (amount_irr > 0),
 status TEXT NOT NULL DEFAULT 'pending'
  CHECK (status IN ('pending','posted','failed','reversed')),
 ledger_transaction_id UUID UNIQUE REFERENCES ledger_transactions(id),
 external_provider TEXT,
 external_reference TEXT,
 idempotency_key TEXT NOT NULL UNIQUE,
 description TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 posted_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_wallet_operations_wallet
 ON wallet_operations(wallet_id, created_at DESC);

-- Planned API: GET /v1/wallet, GET /v1/wallet/transactions,
-- POST /v1/wallet/top-ups, POST /v1/wallet/trip-payments,
-- POST /v1/wallet/refunds, POST /v1/driver/payouts.
-- These endpoints require authenticated server-side implementation.
-- Credit top-ups only after verified server-to-server gateway confirmation.
-- Never trust client balances, prices, redirects or duplicate callbacks.
-- Do not enable peer-to-peer transfers or unrestricted cash-out before legal/provider approval.
