-- ============================================================================
-- Parental Care — Supabase Database Schema (PostgreSQL)
-- ============================================================================
-- Run this in Supabase SQL Editor or via `supabase db push`
-- ============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- Custom Types
-- ============================================================================
DO $$
BEGIN
    CREATE TYPE user_role AS ENUM ('PARENT', 'CHILD');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

DO $$
BEGIN
    CREATE TYPE request_status AS ENUM (
        'REQUESTED', 'PROCESSING', 'UPLOADED', 'DELIVERED',
        'EXPIRED', 'CANCELLED', 'FAILED'
    );
EXCEPTION
    WHEN duplicate_object THEN NULL;
END
$$;

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT NOT NULL UNIQUE,
    display_name TEXT,
    role user_role NOT NULL DEFAULT 'PARENT',
    family_id UUID,
    photo_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE
);

-- Index for auth lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_family_id ON users(family_id);

-- ============================================================================
-- 2. FAMILIES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS families (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    parent_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    retention_hours INT NOT NULL DEFAULT 24,
    biometric_lock_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Add the reverse side of the users <-> families relationship after both
-- tables exist. This avoids a forward-reference/circular creation failure.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_family_id_fkey'
          AND conrelid = 'public.users'::regclass
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT users_family_id_fkey
            FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE SET NULL;
    END IF;
END
$$;

-- ============================================================================
-- 3. MEMBERS TABLE (for multi-parent support)
-- ============================================================================
CREATE TABLE IF NOT EXISTS members (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role user_role NOT NULL DEFAULT 'CHILD',
    display_name TEXT NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(family_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_members_family_id ON members(family_id);
CREATE INDEX IF NOT EXISTS idx_members_user_id ON members(user_id);

-- ============================================================================
-- 4. DEVICES TABLE (Child devices)
-- ============================================================================
CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    owner_member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    child_display_name TEXT NOT NULL,
    device_name TEXT NOT NULL,
    device_model TEXT NOT NULL,
    android_version TEXT NOT NULL,
    fcm_token TEXT,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    paired_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    monitoring_active BOOLEAN NOT NULL DEFAULT TRUE,
    screenshot_count INT NOT NULL DEFAULT 0,
    request_count INT NOT NULL DEFAULT 0,
    last_screenshot_at TIMESTAMPTZ,
    retention_hours INT NOT NULL DEFAULT 24
);

CREATE INDEX IF NOT EXISTS idx_devices_family_id ON devices(family_id);
CREATE INDEX IF NOT EXISTS idx_devices_owner_member_id ON devices(owner_member_id);
CREATE INDEX IF NOT EXISTS idx_devices_fcm_token ON devices(fcm_token) WHERE fcm_token IS NOT NULL;

-- ============================================================================
-- 5. SCREENSHOT REQUESTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS screenshot_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    parent_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    child_device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    nonce TEXT NOT NULL,
    status request_status NOT NULL DEFAULT 'REQUESTED',
    delay_seconds INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMPTZ,
    failure_reason TEXT,
    cancellation_reason TEXT
);

-- PostgreSQL does not support a WHERE clause on a table-level UNIQUE
-- constraint. This index also enforces the intended rule: one active request
-- per family/device, regardless of whether it is REQUESTED or PROCESSING.
CREATE UNIQUE INDEX IF NOT EXISTS idx_screenshot_requests_one_active
    ON screenshot_requests(family_id, child_device_id)
    WHERE status IN ('REQUESTED', 'PROCESSING');

CREATE INDEX IF NOT EXISTS idx_screenshot_requests_family_id ON screenshot_requests(family_id);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_child_device_id ON screenshot_requests(child_device_id);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_parent_user_id ON screenshot_requests(parent_user_id);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_status ON screenshot_requests(status);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_expires_at ON screenshot_requests(expires_at);

-- ============================================================================
-- 6. SCREENSHOTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS screenshots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    parent_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    child_device_id UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    request_id UUID NOT NULL REFERENCES screenshot_requests(id) ON DELETE CASCADE,
    storage_path TEXT NOT NULL UNIQUE,
    iv TEXT NOT NULL,
    wrapped_key TEXT NOT NULL,
    thumbnail_base64 TEXT,
    mime_type TEXT NOT NULL DEFAULT 'image/jpeg',
    width_px INT NOT NULL DEFAULT 0,
    height_px INT NOT NULL DEFAULT 0,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    delivered_at TIMESTAMPTZ,
    viewed_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    retention_expires_at TIMESTAMPTZ NOT NULL,
    is_unread BOOLEAN NOT NULL DEFAULT TRUE,
    encrypted_payload_base64 TEXT
);

CREATE INDEX IF NOT EXISTS idx_screenshots_family_id ON screenshots(family_id);
CREATE INDEX IF NOT EXISTS idx_screenshots_parent_user_id ON screenshots(parent_user_id);
CREATE INDEX IF NOT EXISTS idx_screenshots_child_device_id ON screenshots(child_device_id);
CREATE INDEX IF NOT EXISTS idx_screenshots_request_id ON screenshots(request_id);
CREATE INDEX IF NOT EXISTS idx_screenshots_captured_at ON screenshots(captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_screenshots_retention_expires_at ON screenshots(retention_expires_at);
CREATE INDEX IF NOT EXISTS idx_screenshots_is_unread ON screenshots(is_unread) WHERE is_unread = TRUE;

-- ============================================================================
-- 7. PAIRING TOKENS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS pairing_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token_id TEXT NOT NULL UNIQUE,
    opaque TEXT NOT NULL,
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    parent_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_display_name TEXT NOT NULL,
    parent_email TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    nonce TEXT NOT NULL,
    is_consumed BOOLEAN NOT NULL DEFAULT FALSE,
    consumed_by_device_id TEXT,
    consumed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_pairing_tokens_family_id ON pairing_tokens(family_id);
CREATE INDEX IF NOT EXISTS idx_pairing_tokens_expires_at ON pairing_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_pairing_tokens_is_consumed ON pairing_tokens(is_consumed) WHERE is_consumed = FALSE;

-- ============================================================================
-- 8. ACTIVITY LOG TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS activity_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL,
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_device_id UUID REFERENCES devices(id) ON DELETE SET NULL,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activity_log_family_id ON activity_log(family_id);
CREATE INDEX IF NOT EXISTS idx_activity_log_created_at ON activity_log(created_at DESC);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Policy names are owned by this schema script. Dropping only these named
-- policies makes the policy section safe to rerun without touching unrelated
-- policies that may have been added by the application.
DO $$
BEGIN
    DROP POLICY IF EXISTS "Users can read own profile" ON users;
    DROP POLICY IF EXISTS "Users can update own profile" ON users;

    DROP POLICY IF EXISTS "Parents can read own families" ON families;
    DROP POLICY IF EXISTS "Parents can create families" ON families;
    DROP POLICY IF EXISTS "Parents can update own families" ON families;
    DROP POLICY IF EXISTS "Parents can delete own families" ON families;

    DROP POLICY IF EXISTS "Parents can read family members" ON members;
    DROP POLICY IF EXISTS "Parents can manage family members" ON members;

    DROP POLICY IF EXISTS "Parents can read family devices" ON devices;
    DROP POLICY IF EXISTS "Device can read own record" ON devices;
    DROP POLICY IF EXISTS "Device can create own record" ON devices;
    DROP POLICY IF EXISTS "Owners can update devices" ON devices;
    DROP POLICY IF EXISTS "Parents can delete devices" ON devices;

    DROP POLICY IF EXISTS "Parents can read family requests" ON screenshot_requests;
    DROP POLICY IF EXISTS "Device can read own pending requests" ON screenshot_requests;
    DROP POLICY IF EXISTS "Parents can create requests" ON screenshot_requests;
    DROP POLICY IF EXISTS "Valid status transitions" ON screenshot_requests;
    DROP POLICY IF EXISTS "Parents can delete requests" ON screenshot_requests;

    DROP POLICY IF EXISTS "Parents can read family screenshots" ON screenshots;
    DROP POLICY IF EXISTS "Device can upload screenshots" ON screenshots;
    DROP POLICY IF EXISTS "Parents can update screenshot metadata" ON screenshots;
    DROP POLICY IF EXISTS "Parents can delete screenshots" ON screenshots;

    DROP POLICY IF EXISTS "Parents can create pairing tokens" ON pairing_tokens;
    DROP POLICY IF EXISTS "Valid tokens can be read" ON pairing_tokens;
    DROP POLICY IF EXISTS "Token can be consumed" ON pairing_tokens;

    DROP POLICY IF EXISTS "Parents can read family activity" ON activity_log;
    DROP POLICY IF EXISTS "Family members can write activity" ON activity_log;

    DROP POLICY IF EXISTS "Device can upload screenshots" ON storage.objects;
    DROP POLICY IF EXISTS "Parent can read family screenshots" ON storage.objects;
    DROP POLICY IF EXISTS "Parent can delete family screenshots" ON storage.objects;
END
$$;

-- Helper function to get current user's auth.uid()
-- Note: In Supabase, auth.uid() returns the authenticated user's UUID

-- ============================================================================
-- USERS RLS
-- ============================================================================
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Users can read their own profile
CREATE POLICY "Users can read own profile" ON users
    FOR SELECT USING (auth.uid() = id);

-- Users can update their own profile (limited fields)
CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id AND role = (SELECT role FROM users WHERE id = auth.uid()));

-- Service role can insert users (handled by auth trigger)
-- No DELETE policy - users deleted via admin only

-- ============================================================================
-- FAMILIES RLS
-- ============================================================================
ALTER TABLE families ENABLE ROW LEVEL SECURITY;

-- Parents can read families they own
CREATE POLICY "Parents can read own families" ON families
    FOR SELECT USING (parent_user_id = auth.uid());

-- Parents can create families
CREATE POLICY "Parents can create families" ON families
    FOR INSERT WITH CHECK (parent_user_id = auth.uid());

-- Parents can update their families (limited fields)
CREATE POLICY "Parents can update own families" ON families
    FOR UPDATE USING (parent_user_id = auth.uid())
    WITH CHECK (parent_user_id = auth.uid());

-- Parents can delete their families
CREATE POLICY "Parents can delete own families" ON families
    FOR DELETE USING (parent_user_id = auth.uid());

-- ============================================================================
-- MEMBERS RLS
-- ============================================================================
ALTER TABLE members ENABLE ROW LEVEL SECURITY;

-- Parents can read all members in their families
CREATE POLICY "Parents can read family members" ON members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = members.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- Parents can manage members in their families
CREATE POLICY "Parents can manage family members" ON members
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = members.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- ============================================================================
-- DEVICES RLS
-- ============================================================================
ALTER TABLE devices ENABLE ROW LEVEL SECURITY;

-- Parents can read all devices in their families
CREATE POLICY "Parents can read family devices" ON devices
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = devices.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- Child device can read ONLY its own device record
CREATE POLICY "Device can read own record" ON devices
    FOR SELECT USING (
        owner_member_id = auth.uid()
    );

-- Child device can create its own record during pairing
CREATE POLICY "Device can create own record" ON devices
    FOR INSERT WITH CHECK (
        owner_member_id = auth.uid()
    );

-- Parents can update devices in their families (all fields)
-- Child device can update its own record (limited fields)
CREATE POLICY "Owners can update devices" ON devices
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = devices.family_id 
            AND f.parent_user_id = auth.uid()
        ) OR owner_member_id = auth.uid()
    )
    WITH CHECK (
        (EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = devices.family_id 
            AND f.parent_user_id = auth.uid()
        ) OR owner_member_id = auth.uid())
        AND family_id = (SELECT family_id FROM devices WHERE id = devices.id)
        AND owner_member_id = (SELECT owner_member_id FROM devices WHERE id = devices.id)
    );

-- Parents can delete devices
CREATE POLICY "Parents can delete devices" ON devices
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = devices.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- ============================================================================
-- SCREENSHOT REQUESTS RLS
-- ============================================================================
ALTER TABLE screenshot_requests ENABLE ROW LEVEL SECURITY;

-- Parents can read all requests in their families
CREATE POLICY "Parents can read family requests" ON screenshot_requests
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshot_requests.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- Child device can read requests addressed to it (not expired)
CREATE POLICY "Device can read own pending requests" ON screenshot_requests
    FOR SELECT USING (
        child_device_id = auth.uid()
        AND expires_at > NOW()
    );

-- Parents can create requests for devices in their families
CREATE POLICY "Parents can create requests" ON screenshot_requests
    FOR INSERT WITH CHECK (
        parent_user_id = auth.uid()
        AND EXISTS (
            SELECT 1 FROM devices d 
            WHERE d.id = screenshot_requests.child_device_id 
            AND d.family_id = screenshot_requests.family_id
        )
        AND status = 'REQUESTED'
        AND expires_at - created_at <= INTERVAL '30 minutes'
        AND nonce IS NOT NULL AND length(nonce) >= 16
    );

-- Status transitions
-- Parents: REQUESTED -> DELIVERED, CANCELLED
-- Child: REQUESTED -> PROCESSING, UPLOADED, FAILED
CREATE POLICY "Valid status transitions" ON screenshot_requests
    FOR UPDATE USING (
        (EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshot_requests.family_id 
            AND f.parent_user_id = auth.uid()
        ) AND status IN ('REQUESTED', 'DELIVERED', 'CANCELLED'))
        OR 
        (child_device_id = auth.uid() AND status IN ('REQUESTED', 'PROCESSING', 'UPLOADED', 'FAILED'))
    )
    WITH CHECK (
        family_id = (SELECT family_id FROM screenshot_requests WHERE id = screenshot_requests.id)
        AND child_device_id = (SELECT child_device_id FROM screenshot_requests WHERE id = screenshot_requests.id)
        AND parent_user_id = (SELECT parent_user_id FROM screenshot_requests WHERE id = screenshot_requests.id)
        AND nonce = (SELECT nonce FROM screenshot_requests WHERE id = screenshot_requests.id)
        AND expires_at = (SELECT expires_at FROM screenshot_requests WHERE id = screenshot_requests.id)
        AND status IN ('REQUESTED', 'PROCESSING', 'UPLOADED', 'DELIVERED', 'EXPIRED', 'CANCELLED', 'FAILED')
    );

-- Parents can delete requests
CREATE POLICY "Parents can delete requests" ON screenshot_requests
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshot_requests.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- ============================================================================
-- SCREENSHOTS RLS
-- ============================================================================
ALTER TABLE screenshots ENABLE ROW LEVEL SECURITY;

-- Parents can read all screenshots in their families
CREATE POLICY "Parents can read family screenshots" ON screenshots
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshots.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- Child device can CREATE screenshots (upload)
CREATE POLICY "Device can upload screenshots" ON screenshots
    FOR INSERT WITH CHECK (
        child_device_id = auth.uid()
        AND EXISTS (
            SELECT 1 FROM screenshot_requests sr
            WHERE sr.id = screenshots.request_id
            AND sr.family_id = screenshots.family_id
            AND sr.parent_user_id = screenshots.parent_user_id
            AND sr.status IN ('REQUESTED', 'PROCESSING')
        )
        AND storage_path ~ '^families/[^/]+/screenshots/[^/]+/[^/]+\.enc$'
        AND iv IS NOT NULL
        AND wrapped_key IS NOT NULL
        AND retention_expires_at > NOW()
    );

-- Parents can update limited metadata
CREATE POLICY "Parents can update screenshot metadata" ON screenshots
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshots.family_id 
            AND f.parent_user_id = auth.uid()
        )
    )
    WITH CHECK (
        viewed_at IS NOT NULL OR is_unread = FALSE OR delivered_at IS NOT NULL
    );

-- Parents can delete screenshots
CREATE POLICY "Parents can delete screenshots" ON screenshots
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = screenshots.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- ============================================================================
-- PAIRING TOKENS RLS
-- ============================================================================
ALTER TABLE pairing_tokens ENABLE ROW LEVEL SECURITY;

-- Parent can create tokens
CREATE POLICY "Parents can create pairing tokens" ON pairing_tokens
    FOR INSERT WITH CHECK (
        parent_user_id = auth.uid()
        AND is_consumed = FALSE
        AND expires_at - created_at < INTERVAL '5 minutes'
    );

-- Anyone with token can read (within TTL, not consumed)
CREATE POLICY "Valid tokens can be read" ON pairing_tokens
    FOR SELECT USING (
        is_consumed = FALSE 
        AND expires_at > NOW()
    );

-- Token can be marked consumed (by child device during pairing)
CREATE POLICY "Token can be consumed" ON pairing_tokens
    FOR UPDATE USING (
        is_consumed = FALSE 
        AND expires_at > NOW()
    )
    WITH CHECK (
        is_consumed = TRUE
    );

-- ============================================================================
-- ACTIVITY LOG RLS
-- ============================================================================
ALTER TABLE activity_log ENABLE ROW LEVEL SECURITY;

-- Parents can read all activity in their families
CREATE POLICY "Parents can read family activity" ON activity_log
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = activity_log.family_id 
            AND f.parent_user_id = auth.uid()
        )
    );

-- Both parents and child devices can write activity
CREATE POLICY "Family members can write activity" ON activity_log
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = activity_log.family_id 
            AND (f.parent_user_id = auth.uid() 
                 OR EXISTS (SELECT 1 FROM devices d WHERE d.id = auth.uid() AND d.family_id = f.id))
        )
        AND family_id = activity_log.family_id
    );

-- ============================================================================
-- STORAGE BUCKET & POLICIES
-- ============================================================================

-- Keep the bucket private. Storage size and MIME restrictions should also be
-- configured here rather than relying only on policy expressions.
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'screenshots',
    'screenshots',
    FALSE,
    2097152,
    ARRAY['application/octet-stream']::TEXT[]
)
ON CONFLICT (id) DO NOTHING;

-- Storage policies (match the path pattern: families/{familyId}/screenshots/{childDeviceId}/{screenshotId}.enc)

-- Child device can WRITE screenshots to its own path
CREATE POLICY "Device can upload screenshots" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'screenshots'
        AND (storage.foldername(name))[1] = 'families'
        AND (storage.foldername(name))[3] = 'screenshots'
        AND (storage.foldername(name))[4] = auth.uid()::text
        -- Storage objects do not expose content/content_type columns. Supabase
        -- stores upload metadata in the metadata JSONB column.
        AND CASE
            WHEN (metadata->>'size') ~ '^[0-9]+$'
                THEN (metadata->>'size')::BIGINT <= 2097152
            ELSE FALSE
        END
        AND metadata->>'mimetype' = 'application/octet-stream'
        AND name ~ '^families/[^/]+/screenshots/[^/]+/[^/]+\.enc$'
    );

-- Parent can READ screenshots in their family
CREATE POLICY "Parent can read family screenshots" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'screenshots'
        AND (storage.foldername(name))[1] = 'families'
        AND (storage.foldername(name))[2] IN (
            SELECT id::text FROM families WHERE parent_user_id = auth.uid()
        )
    );

-- Parent can DELETE screenshots in their family
CREATE POLICY "Parent can delete family screenshots" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'screenshots'
        AND (storage.foldername(name))[1] = 'families'
        AND (storage.foldername(name))[2] IN (
            SELECT id::text FROM families WHERE parent_user_id = auth.uid()
        )
    );

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_families_updated_at ON families;
CREATE TRIGGER update_families_updated_at BEFORE UPDATE ON families
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- AUTH TRIGGER (Auto-create user profile on signup)
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, display_name, role, is_email_verified)
    VALUES (NEW.id, NEW.email, NEW.raw_user_meta_data->>'full_name', 'PARENT', NEW.email_confirmed_at IS NOT NULL);
    RETURN NEW;
EXCEPTION WHEN unique_violation THEN
    -- Ignore if already exists
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- GRANTS
-- ============================================================================
GRANT USAGE ON SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL TABLES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO anon, authenticated;
