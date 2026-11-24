# Zaffran Taxi — AI Operations Architecture

## Goal
A compact Persian-first AI assistant coordinates routine work while the platform owner remains the supervisor. AI is an assistant and workflow orchestrator, not an unrestricted authority.

## Modules
- Passenger assistant: onboarding, trip help, FAQs, complaint intake and status updates.
- Driver assistant: registration guidance, document checklist, service category and trip support.
- Document pre-check: OCR, image quality, expiry/date extraction and mismatch flags. A human reviewer makes identity approval decisions.
- Dispatch support: rank eligible nearby drivers using service type, availability, distance and safety constraints. Dispatch rules remain deterministic and auditable.
- Finance assistant: explain wallet activity, reconcile ledger events, flag anomalies and prepare payout batches. It cannot create money, alter balances directly or approve its own transfers.
- Supervisor copilot: daily summaries, exception queues, complaint categorization, driver review queue and operational alerts.
- Safety support: detect emergency keywords and escalate to human support; never delay emergency contact behind an AI conversation.

## Small-model deployment
Use a replaceable Persian-capable quantized model behind an internal API. Keep deterministic business rules in FastAPI services. The app must continue core flows when the model is offline; AI features degrade gracefully. Prefer an Iran-hosted inference endpoint for national-network reachability, subject to an actual provider and connectivity test.

## Human oversight and security
- Owner/supervisor dashboard with audit trail.
- Role-based access and least privilege.
- Sensitive identity and financial data encrypted in transit and at rest.
- AI suggestions are logged with model/version and evidence.
- Human approval required for identity activation, account suspension appeals, refunds above policy thresholds, payout destination changes and exceptional financial actions.
- All payment and wallet changes are validated by server-side ledger transactions and idempotency keys.
- Never expose card numbers, passwords, OTPs or full identity documents to the language model.

## Delivery sequence
1. Build passenger/driver UI and map selection.
2. Implement authenticated backend and audit log.
3. Add document intake and human review queue.
4. Implement wallet ledger and payment-provider adapter.
5. Add AI assistant in read-only mode, then narrowly scoped actions.
6. Add supervisor dashboard, monitoring, tests and staged rollout.

## Current limitations
This document is architecture, not a deployed model. Real AI requires a selected hosting/inference provider, model evaluation in Persian, privacy review and production integration. Map tiles and routing require a provider reachable from target networks; tile display alone is not route calculation or live location tracking.
