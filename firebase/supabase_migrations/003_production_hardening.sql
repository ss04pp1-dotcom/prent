-- Add missing encryption key columns to pairing_tokens
ALTER TABLE pairing_tokens ADD COLUMN IF NOT EXISTS parent_public_key TEXT;
ALTER TABLE pairing_tokens ADD COLUMN IF NOT EXISTS parent_encryption_public_key TEXT;

-- Index Optimization
CREATE INDEX IF NOT EXISTS idx_screenshots_family_id ON screenshots(family_id);
CREATE INDEX IF NOT EXISTS idx_screenshots_request_id ON screenshots(request_id);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_family_id ON screenshot_requests(family_id);
CREATE INDEX IF NOT EXISTS idx_screenshot_requests_status ON screenshot_requests(status);
CREATE INDEX IF NOT EXISTS idx_devices_family_id ON devices(family_id);
CREATE INDEX IF NOT EXISTS idx_devices_owner_member_id ON devices(owner_member_id);

-- Tightening RLS for members
ALTER TABLE members ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Members can read family members" ON members;
CREATE POLICY "Members can read family members" ON members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM families f 
            WHERE f.id = members.family_id 
            AND (f.parent_user_id = auth.uid() OR EXISTS (SELECT 1 FROM devices d WHERE d.id = auth.uid() AND d.family_id = f.id))
        )
    );
