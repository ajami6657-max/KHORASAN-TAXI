# Zaffran Taxi — Iran-Network Resilience Requirements

## Product requirement
The app is online-first, not offline-only, and must avoid dependence on international services. It should remain usable on weak, high-latency and intermittently unavailable mobile networks. It cannot guarantee operation if all connectivity, domestic infrastructure, power or mobile networks are unavailable.

## Deployment
- Host API, primary PostgreSQL database, object storage, AI inference and operational monitoring with providers reachable over Iran's National Information Network.
- Verify provider reachability from multiple Iranian mobile and fixed operators before launch; do not assume an Iran-based IP alone guarantees availability.
- Keep production dependencies replaceable: SMS, maps/tiles, routing, payment gateway, push notifications and AI inference each have adapters and documented fallbacks.
- Use backups and a tested recovery plan in a separate domestic availability zone/provider where feasible.

## Low-bandwidth mobile behavior
- Small initial app bundle; defer nonessential screens and assets.
- Compress images, cap upload size, resize document photos on device and upload resumably in chunks.
- Use pagination, delta sync, compact JSON, request timeouts and exponential backoff with jitter.
- Cache static configuration, service categories, fare rules version, help articles and recently used places.
- Queue safe, idempotent actions locally while disconnected and sync when the connection returns. Show clear pending/sent/failed states.
- Never queue a payment as if it succeeded. Verify payment status with the gateway/server before showing success.
- Avoid continuous high-frequency GPS updates; adapt location frequency to trip state and network quality.

## Map and trip continuity
- Use a map/tile/routing provider contractually reachable within Iran. Keep provider adapters so the map can be changed without rewriting the app.
- Cache recently viewed map tiles and important local areas where licensing permits.
- Allow manual pickup/drop-off address entry and coordinate pin selection if map tiles fail.
- During a live trip, show last known driver position and timestamp when updates are delayed; never present stale location as live.
- Routing, fare estimates and dispatch are server-confirmed. A cached map by itself does not provide live traffic or live driver positions.

## AI behavior
- Run a compact Persian-capable model behind a domestic API, with bounded context and request limits.
- Keep essential workflows independent of the AI service. If AI is unavailable, forms, trip requests, wallet history and human support routes continue.
- AI can classify, summarize and suggest; deterministic backend rules control identity status, fares, wallet ledger and payouts.
- Human review remains required for identity approval and sensitive financial or safety decisions.

## Degraded-mode matrix
| Condition | Expected behavior |
|---|---|
| International internet unavailable, domestic network works | Core app continues through domestic services |
| Slow or intermittent data | Cached content, smaller payloads, retries and queued safe actions |
| Map provider unavailable | Manual address/coordinate selection; show map unavailable |
| AI service unavailable | Core app works; AI assistant reports temporary unavailability |
| Payment gateway unavailable | Do not claim payment succeeded; preserve order and retry status verification |
| Mobile data fully unavailable | No live dispatch, payment verification or driver tracking; show cached info and pending actions only |

## Security and reliability
- Use TLS, short-lived access tokens, device/session controls, rate limiting and audit logs.
- Make trip creation, wallet mutations and payout requests idempotent.
- Use a transactional ledger; never trust client-side balances or client timestamps.
- Monitor API latency, error rates, SMS delivery, map tile access and provider health from inside Iran.
- Run chaos tests for packet loss, high latency, DNS failures, provider outages and app restarts during queued sync.

## Acceptance tests before launch
1. Test on major Iranian mobile operators and fixed broadband from multiple provinces.
2. Test with simulated 2G-like bandwidth, high latency, packet loss and 30–120 second outages.
3. Test app restart while offline and safe queue replay after reconnect.
4. Prove duplicate taps/retries cannot create duplicate trips, charges or payouts.
5. Confirm the app never labels pending payments as paid or stale GPS as live.
6. Conduct a real domestic-network outage drill and document recovery times.

## Important limitation
No app can guarantee live ride matching or online payments during a total communications blackout or loss of power. The design aims to preserve core usability during international isolation and weak/intermittent domestic connectivity, with transparent limits during complete outages.
