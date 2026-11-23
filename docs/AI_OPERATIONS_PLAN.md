# Zaffran Taxi — AI Operations Plan

## Product principle
AI should automate repetitive, high-volume and low-risk work across passenger support, driver support and operational administration. The backend remains the source of truth. AI proposes or executes only explicitly permitted actions through typed, authenticated tools with policy checks, audit logs and idempotency.

## AI capabilities
1. Passenger concierge: Persian natural-language trip planning, service discovery, village/landmark address clarification, booking assistance, FAQs and trip-status explanations.
2. Driver copilot: onboarding guidance, document checklist, trip instructions, support, earnings explanations and navigation handoff.
3. Dispatch assistant: rank eligible nearby drivers using deterministic constraints (service approval, vehicle, availability, capacity, distance, safety and fairness); AI may explain/rank candidates but cannot bypass eligibility or dispatch policy.
4. Support automation: classify tickets, summarize conversations, draft replies, detect urgency and route safety incidents immediately to a human response channel.
5. Operations copilot: summarize demand, supply gaps, cancellations, complaints, service-zone coverage and operational anomalies for admins.
6. Finance assistant: explain ledger entries, commissions, wallet activity and payout states; detect reconciliation anomalies. It cannot create money, alter ledger entries, approve refunds or initiate transfers without deterministic authorization and required human approval.
7. Document intake assistant: OCR, completeness and image-quality checks, expiry reminders and mismatch flags. Final identity/permit approval remains a human decision.
8. Pricing intelligence: forecast demand and recommend fare parameters to admins. Published fares must come from versioned deterministic pricing rules; no opaque AI-only fare changes or discriminatory pricing.
9. Fraud and safety signals: flag suspicious patterns for review; no automatic accusation, punitive suspension or denial solely from model output.
10. Engineering/QA copilot: generate tests, inspect logs, explain build failures and draft code changes; protected branches, review and CI gates remain required.

## Autonomy levels
- Level 0 — Explain: answer from approved help and authenticated read-only data.
- Level 1 — Draft: prepare messages, summaries, fare/dispatch recommendations and admin actions for review.
- Level 2 — Bounded execution: execute reversible, low-risk actions such as FAQ responses, ticket tagging, reminders and non-financial profile edits, within rate limits.
- Level 3 — Approval-gated: refunds, account restrictions, driver approval, fare-rule publication, wallet adjustments, payout initiation and safety-related account actions require authorized human approval or a formally approved deterministic workflow.
- Never autonomous: inventing trip/payment states, changing ledger history, bypassing identity/safety checks, making legal eligibility decisions, or claiming a bank transfer has settled without provider confirmation.

## Technical architecture
- AI gateway service separate from core trip/finance services.
- Model adapter supports a replaceable Persian-capable model, including an Iran-hosted option after latency, license, privacy and cost testing.
- Retrieval only from approved versioned policies/FAQs; private account/trip information is fetched through permission-checked backend tools, never model memory.
- Strict JSON schemas and allow-listed tools; validate every tool argument server-side.
- Role-based access, least privilege, per-action authorization, audit trails, redaction of personal data, retention limits and prompt-injection defenses.
- Human handoff must preserve conversation context and clearly disclose when AI is assisting.
- Graceful fallback to deterministic flows and human support if AI or connectivity is unavailable.

## Success measures
Track containment rate, first-response time, resolution time, booking completion, dispatch acceptance, cancellation rate, support escalation quality, false-positive rate, payout reconciliation exceptions, latency and cost per completed trip. Do not optimize automation rate at the expense of safety or fairness.

## Delivery sequence
1. Read-only Persian assistant for FAQs and authenticated trip/wallet status.
2. Support ticket classification, summaries and suggested responses.
3. Driver onboarding assistant and document completeness checks with human review.
4. Dispatch candidate ranking and operational dashboards.
5. Approval-gated finance/support workflows after ledger, auth, audit and reconciliation tests.
6. Demand forecasting and admin-reviewed pricing recommendations.

## Explicit limitations
AI can make mistakes. It must not be the sole authority for identity, safety, legal eligibility, fare calculation, money movement, refunds, wallet balances, or payment settlement. All critical business rules are enforced by backend code and database constraints, not by prompts.

## Compact central AI supervisor model

Use one compact Persian-capable model behind a single AI gateway, with narrowly scoped tools for passenger support, driver onboarding, document intake, trip operations and finance explanations. Keep the model replaceable and benchmark a small quantized model on the intended server before selecting it; do not assume a phone-sized model can safely run all production workflows.

The model is the coordinator, not the database or financial authority:
- Identity: collect/check document completeness, OCR fields and flag mismatches; a human supervisor approves or rejects identity and driver activation.
- Finance: read verified ledger/payment/payout states, explain commissions and prepare reconciliation exceptions; backend rules calculate every amount. Refunds, wallet adjustments and bank transfers require an authorized approval and provider confirmation.
- Operations: answer routine questions, classify support tickets, draft replies, remind users and recommend eligible drivers; the backend enforces permissions and trip rules.
- Supervision: provide a dedicated admin dashboard with pending identity reviews, payment/payout exceptions, safety escalations, AI actions, confidence/reason summaries and an immutable audit trail.

Every AI action must include actor, role, tool, target record, timestamp, input/output summary, policy decision and approval reference. Use least-privilege service accounts, short-lived credentials, rate limits, redaction and deterministic fallbacks. Never give the model unrestricted database, banking, identity-provider or production-shell access.
