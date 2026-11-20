# Zaffran Taxi — Driver Payout Design

## Decision
- Card payment gateway selected by the product owner: Asan Pardakht (AP / آپ).
- Driver payout options: instant request and daily batch.
- Both options are product requirements. Real bank transfer activation is gated by provider contracts, official API documentation, compliance approval and sandbox/production verification.

## Money flow
1. Passenger pays the quoted trip fare through the AP adapter.
2. Backend verifies the transaction server-to-server; browser/app redirect alone never confirms payment.
3. On trip completion, ledger posts gross collection, platform commission, driver payable and any explicitly disclosed fee as balanced, immutable entries.
4. Available driver balance is computed from ledger entries minus pending payouts, holds, reversals and adjustments.
5. A payout request reserves the amount atomically before calling the payout provider.
6. Only a verified final success marks it paid. Pending/unknown responses remain pending and are reconciled; retries reuse the same idempotency key.
7. Rejected/failed/returned payouts release the reservation exactly once.

## Driver settings
- preferred_payout_mode: instant | daily
- verified_bank_account: bank name, masked IBAN, ownership-verification state
- payout history: amount, fee, request time, status, provider reference and final settlement time

Drivers may choose a preference, but eligibility, limits and availability are controlled server-side. Admin can pause payouts or change limits with an audit reason.

## Instant mode
- Driver initiates a withdrawal from eligible available balance.
- Enforce provider/bank limits, minimum balance, fees, daily count/amount limits and risk checks.
- Show states: requested, processing, paid, rejected, failed, returned.
- Never label an accepted request as paid before final confirmation.
- Activate only if AP or an approved payout partner contractually supports the transfer rail, eligible banks, API access, operating hours and SLA.

## Daily mode
- Scheduler creates one batch per configured business day and cutoff.
- Respect holidays, bank settlement calendars, minimum payout, provider limits and retry policy.
- Show estimated payment date/time separately from confirmed bank settlement.
- Admin can review batch totals and reconcile each transfer.

## Security and accounting controls
- Server-side calculations only; amounts stored as integer IRR (or a single explicitly chosen minor unit), never floating point.
- Unique idempotency key for every payment, ledger posting and payout.
- Double-entry ledger; immutable postings with compensating entries for refunds/adjustments.
- Verify callbacks and query provider status server-to-server.
- Prevent duplicate payout under concurrent requests using database transactions/row locks.
- Verify bank-account ownership and protect IBANs as sensitive data.
- Role-based admin approval, maker-checker for manual adjustments, full audit trail.
- Daily reconciliation against AP and bank/payout reports.

## Open integration gates
1. Obtain AP merchant onboarding and official technical documents.
2. Confirm whether AP supports marketplace/split settlement and whether it can pay multiple driver beneficiaries.
3. If not, select an approved bank-transfer/payout partner for driver disbursement.
4. Confirm legal structure, merchant-of-record, tax/invoicing, driver KYC, fund custody and refund/chargeback responsibilities with qualified counsel and providers.
5. Test callbacks, duplicate events, timeout/unknown status, partial failure, returned transfers and reconciliation in sandbox before production.

## Current implementation status
This document records the agreed product design. It does not mean AP credentials, payout APIs, bank contracts, backend endpoints or real transfers are implemented. No secrets or bank credentials belong in the repository.
