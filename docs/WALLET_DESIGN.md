# Zaffran Taxi Wallet Design

## Passenger wallet
- Balance and paginated transaction history.
- Top up through the approved gateway; credit only after server-to-server verification.
- Pay eligible trip fares from wallet funds.
- Receive eligible trip refunds under the published cancellation/refund policy.
- Receipts and clear pending/failed states.

## Driver wallet
- Separate trip earnings, commission, pending/held and available payable amounts.
- Payout preference: instant request or daily batch.
- Bank destination verification and payout history.
- A displayed ledger balance is not proof that a bank transfer has settled.

## Accounting and security
- Balances are derived from immutable double-entry ledger postings.
- Store money as integer IRR, never floating point.
- Passenger and driver accounts are separate and access-controlled.
- Post top-ups, trip charges, refunds and payout reservations atomically with idempotency keys.
- Pending gateway transactions are not spendable.
- Corrections use compensating entries; retain audit history and user-visible receipts.
- Never trust client-supplied balances, fares, payment redirects or success claims.

## Legal boundary
Initial wallet scope is internal ledger-backed credit/payment accounting. Peer-to-peer transfers, unrestricted cash-out, deposits, interest, and claims that this is a bank account or stored-value product are excluded until legal and payment-provider review approves the model. Real top-ups and driver cash-outs require signed provider contracts and production verification.

## Planned API (not yet implemented)
- GET /v1/wallet
- GET /v1/wallet/transactions
- POST /v1/wallet/top-ups
- POST /v1/wallet/trip-payments
- POST /v1/wallet/refunds
- POST /v1/driver/payouts
