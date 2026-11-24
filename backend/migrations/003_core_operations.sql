-- Zaffran Taxi core operations foundation
-- No commission rate is defined here. Financial policy is configured separately.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS passenger_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_subject TEXT UNIQUE,
    phone_e164 TEXT UNIQUE,
    display_name TEXT,
    status TEXT NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending','active','blocked','deleted')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS driver_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_subject TEXT UNIQUE,
    phone_e164 TEXT UNIQUE,
    display_name TEXT,
    onboarding_status TEXT NOT NULL DEFAULT 'draft'
        CHECK (onboarding_status IN ('draft','submitted','under_review','approved','rejected','suspended')),
    service_area_code TEXT,
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    last_location_lat DOUBLE PRECISION,
    last_location_lon DOUBLE PRECISION,
    location_updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (last_location_lat IS NULL OR last_location_lat BETWEEN -90 AND 90),
    CHECK (last_location_lon IS NULL OR last_location_lon BETWEEN -180 AND 180)
);

CREATE TABLE IF NOT EXISTS driver_vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES driver_profiles(id),
    vehicle_type TEXT NOT NULL
        CHECK (vehicle_type IN ('sedan','motorcycle','pickup','tow_truck','van','other')),
    make_model TEXT,
    production_year SMALLINT,
    plate_number TEXT NOT NULL,
    color TEXT,
    approval_status TEXT NOT NULL DEFAULT 'pending'
        CHECK (approval_status IN ('pending','approved','rejected','suspended')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(plate_number)
);

CREATE TABLE IF NOT EXISTS driver_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES driver_profiles(id),
    vehicle_id UUID REFERENCES driver_vehicles(id),
    document_type TEXT NOT NULL
        CHECK (document_type IN ('national_id','driving_license','vehicle_card','insurance','inspection','other')),
    object_key TEXT NOT NULL,
    review_status TEXT NOT NULL DEFAULT 'pending'
        CHECK (review_status IN ('pending','ai_flagged','under_review','approved','rejected','expired')),
    expires_at DATE,
    reviewed_by TEXT,
    review_note TEXT,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_driver_documents_review
    ON driver_documents(review_status, uploaded_at);

CREATE TABLE IF NOT EXISTS ride_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    passenger_id UUID NOT NULL REFERENCES passenger_profiles(id),
    service_type TEXT NOT NULL
        CHECK (service_type IN ('taxi','women','shared','intercity','courier','pickup','tow_truck')),
    trip_mode TEXT NOT NULL DEFAULT 'one_way'
        CHECK (trip_mode IN ('one_way','round_trip','scheduled')),
    origin_label TEXT NOT NULL,
    origin_lat DOUBLE PRECISION NOT NULL,
    origin_lon DOUBLE PRECISION NOT NULL,
    destination_label TEXT NOT NULL,
    destination_lat DOUBLE PRECISION NOT NULL,
    destination_lon DOUBLE PRECISION NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    scheduled_for TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'searching'
        CHECK (status IN ('draft','searching','offered','accepted','driver_arriving','in_progress','completed','cancelled','no_driver')),
    quoted_fare_irr BIGINT CHECK (quoted_fare_irr IS NULL OR quoted_fare_irr >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'IRR',
    idempotency_key TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (origin_lat BETWEEN -90 AND 90 AND destination_lat BETWEEN -90 AND 90),
    CHECK (origin_lon BETWEEN -180 AND 180 AND destination_lon BETWEEN -180 AND 180)
);
CREATE INDEX IF NOT EXISTS idx_ride_requests_dispatch
    ON ride_requests(status, requested_at DESC);

CREATE TABLE IF NOT EXISTS ride_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_request_id UUID NOT NULL REFERENCES ride_requests(id),
    driver_id UUID NOT NULL REFERENCES driver_profiles(id),
    status TEXT NOT NULL DEFAULT 'sent'
        CHECK (status IN ('sent','accepted','declined','expired','cancelled')),
    offered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    UNIQUE(ride_request_id, driver_id)
);
CREATE INDEX IF NOT EXISTS idx_ride_offers_driver
    ON ride_offers(driver_id, status, expires_at);

CREATE TABLE IF NOT EXISTS trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ride_request_id UUID NOT NULL UNIQUE REFERENCES ride_requests(id),
    passenger_id UUID NOT NULL REFERENCES passenger_profiles(id),
    driver_id UUID NOT NULL REFERENCES driver_profiles(id),
    vehicle_id UUID REFERENCES driver_vehicles(id),
    status TEXT NOT NULL DEFAULT 'accepted'
        CHECK (status IN ('accepted','driver_arriving','passenger_onboard','in_progress','completed','cancelled','disputed')),
    final_fare_irr BIGINT CHECK (final_fare_irr IS NULL OR final_fare_irr >= 0),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_trips_driver_history
    ON trips(driver_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_trips_passenger_history
    ON trips(passenger_id, created_at DESC);

CREATE TABLE IF NOT EXISTS trip_events (
    id BIGSERIAL PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips(id),
    event_type TEXT NOT NULL,
    actor_type TEXT NOT NULL CHECK (actor_type IN ('passenger','driver','admin','system','ai')),
    actor_id TEXT,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_trip_events_trip
    ON trip_events(trip_id, created_at);

-- Production requirements:
-- * Store document bytes in private object storage; object_key is never a public URL.
-- * Enforce authenticated ownership and role checks in the API.
-- * AI document checks are advisory; high-impact identity/eligibility decisions
--   require a governed review path and an appeal mechanism.
-- * Use a transactional outbox for dispatch notifications and idempotent workers.
-- * Never mark a ride, payment or payout successful from a client-side claim.
