-- 1. Enable pgcrypto for SHA-256 hashing
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. Create the RPC function for Atomic Pairing
CREATE OR REPLACE FUNCTION consume_pairing_token(
  p_token_id TEXT,
  p_opaque_raw TEXT,
  p_nonce TEXT,
  p_device_name TEXT,
  p_device_model TEXT,
  p_android_version TEXT,
  p_fcm_token TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
  v_token RECORD;
  v_opaque_hash TEXT;
  v_device_id UUID;
  v_result JSONB;
BEGIN
  -- Hash the raw opaque value using pgcrypto (sha256)
  v_opaque_hash := encode(digest(p_opaque_raw, 'sha256'), 'hex');

  -- Find the token securely
  SELECT * INTO v_token
  FROM pairing_tokens
  WHERE token_id = p_token_id 
    AND opaque = v_opaque_hash 
    AND nonce = p_nonce
    AND is_consumed = false 
    AND expires_at > now()
  FOR UPDATE; -- Prevents Race Condition

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Invalid, expired, or already consumed pairing token';
  END IF;

  -- Mark token as consumed
  UPDATE pairing_tokens
  SET is_consumed = true,
      consumed_at = now()
  WHERE id = v_token.id;

  -- Create a new device record for the child
  v_device_id := uuid_generate_v4();
  
  INSERT INTO devices (
    id,
    device_id,
    family_id,
    owner_member_id,
    child_display_name,
    device_name,
    device_model,
    android_version,
    fcm_token,
    is_online,
    last_seen_at,
    paired_at,
    monitoring_active,
    screenshot_count,
    request_count
  ) VALUES (
    v_device_id,
    v_device_id::text,
    v_token.family_id,
    v_token.parent_user_id::text, 
    p_device_name,
    p_device_name,
    p_device_model,
    p_android_version,
    p_fcm_token,
    true,
    (extract(epoch from now()) * 1000)::bigint,
    (extract(epoch from now()) * 1000)::bigint,
    true,
    0,
    0
  );
  
  UPDATE pairing_tokens
  SET consumed_by_device_id = v_device_id::text
  WHERE id = v_token.id;

  -- Map the result to camelCase so Android's Kotlin code can parse it correctly
  SELECT jsonb_build_object(
      'deviceId', device_id,
      'familyId', family_id,
      'ownerMemberId', owner_member_id,
      'childDisplayName', child_display_name,
      'deviceName', device_name,
      'deviceModel', device_model,
      'androidVersion', android_version,
      'fcmToken', fcm_token,
      'isOnline', is_online,
      'lastSeenAt', last_seen_at,
      'pairedAt', paired_at,
      'monitoringActive', monitoring_active,
      'screenshotCount', screenshot_count,
      'requestCount', request_count
  ) INTO v_result
  FROM devices
  WHERE id = v_device_id;

  RETURN v_result;
END;
$$;

-- 3. Create screenshots bucket if not exists
INSERT INTO storage.buckets (id, name, public) 
VALUES ('screenshots', 'screenshots', false)
ON CONFLICT (id) DO NOTHING;

-- 4. Policy: Only Authenticated Users can read/write their family's screenshots
CREATE POLICY "Family members can upload/download screenshots"
ON storage.objects FOR ALL
TO authenticated
USING ( bucket_id = 'screenshots' AND auth.uid() IS NOT NULL );
