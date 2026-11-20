# Zaffran Taxi — Product & Technical Specification

**Brand:** ZAFFRAN TAXI / تاکسی زعفران  
**Slogan:** از روستای تو تا مقصد تو  
**Initial launch region:** South Khorasan, Iran  
**Platforms:** Android + iOS, with a shared backend and web admin  
**Budget principle:** Free-first; no paid dependency until its necessity and local availability are verified.

## Product goal

Build a real, safety-conscious mobility and local transport platform inspired by familiar ride-hailing workflows. Reuse common interaction patterns, but use original branding, UI, code, and service operations.

## Applications and roles

1. **Passenger app:** phone sign-in, pickup/drop-off by GPS, map pin or written village address, fare estimate, ride request, trip tracking, receipt, rating, support and complaint.
2. **Driver/provider app:** onboarding, identity and vehicle documents, service categories, availability, trip offers, navigation handoff, trip status, earnings and settlement history.
3. **Admin web panel:** user/provider review, document review queue, service areas, fare rules, trip monitoring, complaints, refunds, payout reconciliation and audit log.
4. **Backend:** authoritative identity, eligibility, fare quotes, dispatch, trip state, payment ledger, settlement and audit records.
5. **Persian AI assistant:** general support and guided workflows; access to private trip/account data only through authenticated, permission-checked backend tools.

## Service catalogue

- Standard taxi: city rides and rural ↔ city rides.
- Women-focused service: women passengers may request eligible women drivers where available; never promise availability where there are no approved providers. Provide privacy controls and safety escalation.
- Shared ride: opt-in only, with route compatibility, seat capacity, transparent per-seat price and passenger consent.
- Intercity taxi: scheduled or immediate, with route-specific fare and trip rules.
- Courier: motorcycle and eligible car delivery.
- Pickup / cargo pickup.
- Tow truck / roadside recovery.

Each provider account must be approved for each service category and vehicle type separately. Do not infer a provider's eligibility for one category from approval in another.

## Geography and weak-connectivity design

- Begin with South Khorasan; enable counties, cities, villages and service zones through admin-managed data, not hard-coded screens.
- Support GPS, map pin, and manual village/landmark/address entry.
- Cache trip drafts and essential reference data locally.
- Use compact API payloads, retry with backoff, request idempotency, and clear offline/pending states.
- GPS and cached maps can help offline, but dispatch, live tracking, online payment and remote AI require a working connection to the relevant service.
- Map, SMS, hosting and payment providers must be tested from Iranian networks before selection. Do not claim national-network availability until tested.

## Recommended architecture

- **Mobile:** Flutter (one codebase for Android and iOS).
- **API:** Python FastAPI, versioned REST endpoints.
- **Database:** PostgreSQL with PostGIS for service areas and geo queries.
- **Realtime:** WebSocket or a locally reachable compatible realtime service, with polling fallback on weak connections.
- **Maps:** MapLibre client with a map tile/routing provider whose coverage, licensing and reachability in Iran have been verified.
- **Admin:** responsive web application.
- **Deployment:** Iranian-hosted environment only after provider, data protection, uptime, backup and cost checks.
- **CI:** GitHub Actions for Android builds; iOS archive/signing requires macOS/Xcode and Apple signing credentials.

The existing repository started as a Kotlin/Jetpack Compose Android prototype. Moving to Android + iOS requires introducing a Flutter application (or maintaining two native clients). Flutter is the selected direction for minimizing duplicated mobile development. Keep the existing Android prototype until the Flutter app builds and its core flows have been tested.

## Core backend modules

- Auth & OTP
- Profiles and roles
- Driver/provider onboarding and document review
- Vehicle and service-category eligibility
- Geography and service zones
- Fare quote and pricing rules
- Dispatch and trip lifecycle
- Location/realtime gateway
- Payment provider adapter
- Double-entry financial ledger and payout reconciliation
- Support, complaints and safety incidents
- AI assistant gateway and audit log

## Payment and commission model

The server is the source of truth for every amount. Never accept a client-calculated fare or a client-declared successful payment.

For each completed and paid trip:
1. Verify the payment provider's signed callback/server-side status.
2. Record the gross amount once using an idempotency key.
3. Create immutable ledger entries for provider payable, platform commission, and any separately disclosed support/service fee.
4. Request split settlement only if the contracted payment provider explicitly supports the required beneficiary settlement flow.
5. Reconcile provider settlement reports and show pending/paid/failed states.

Do not promise instantaneous bank/card transfer. Actual timing depends on the payment provider, acquiring contract, bank rails and compliance checks. Never hold customer funds or operate a wallet without confirming the applicable legal and provider requirements.

## Identity, safety and privacy

- AI may perform document OCR, image-quality checks and mismatch/risk flagging as decision support.
- AI must not be the sole final authority for driver identity, criminal background, license validity or eligibility.
- Require human review for ambiguous/high-risk cases and provide an appeal/correction path.
- Encrypt identity documents at rest and in transit; use short-lived access links and strict role-based access.
- Collect only necessary data; define retention and deletion rules before launch.
- Women-focused service requires operational safety procedures, incident escalation and provider availability controls.

## AI assistant

- Use a replaceable model adapter so the system is not tied to one foreign API.
- Prefer a Persian-capable model that can be hosted in Iran or run on controlled infrastructure.
- Ground trip-specific answers in authenticated backend data; never let the model invent fares, driver status, payment state or booking confirmation.
- Provide a non-AI rules/FAQ fallback.
- Verify network reachability, model license, hardware needs, latency and operating cost before choosing a model.

## Delivery milestones

### Milestone 0 — Foundation
- [x] Existing Android prototype and CI workflow inspected.
- [x] Product scope and safety/finance boundaries documented.
- [ ] Flutter workspace and Android/iOS build pipeline.

### Milestone 1 — Usable MVP
- [ ] Passenger and provider sign-in/profile.
- [ ] Manual/GPS/map-pin origin and destination.
- [ ] Standard taxi ride request and provider acceptance.
- [ ] Server-side fare quote and trip state.
- [ ] Admin review of providers and trips.
- [ ] Test environment and end-to-end tests.

### Milestone 2 — Regional operations
- [ ] Rural/village address directory and service zones.
- [ ] Intercity and scheduled trips.
- [ ] Women-focused service and safety workflow.
- [ ] Driver/provider document review and audit.

### Milestone 3 — Other transport categories
- [ ] Courier, pickup and tow truck workflows.
- [ ] Shared rides, only after seat/route and safety rules are validated.

### Milestone 4 — Money and AI
- [ ] Payment provider integration after local contract and sandbox testing.
- [ ] Ledger, commission, reconciliation and settlement.
- [ ] Persian AI assistant with a verified Iran-accessible deployment.
- [ ] AI-assisted document triage with human final approval.

## Release gates

No public launch until real-device tests, security review, fare/payment reconciliation, driver support operations, incident handling, legal/permit review, privacy terms and provider coverage are complete. Demo mode must be visibly labelled and must never simulate a real ride or payment as successful.
