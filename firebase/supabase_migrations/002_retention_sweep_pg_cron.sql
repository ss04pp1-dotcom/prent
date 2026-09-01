-- ============================================================================
-- Parental Care — pg_cron Retention Sweep
-- ============================================================================
-- Run this in Supabase SQL Editor after enabling pg_cron extension
-- ============================================================================

-- Enable pg_cron before creating the schedule. On Supabase this may require
-- enabling the extension in the dashboard first if the SQL role cannot create
-- extensions.
CREATE EXTENSION IF NOT EXISTS pg_cron;

-- ============================================================================
-- RETENTION SWEEP FUNCTION
-- ============================================================================
-- Soft-deletes expired screenshots in the database and queues their exact
-- Storage paths for deletion by a trusted worker using the Storage API.
-- Storage files must not be deleted by writing directly to storage.objects.
-- Runs daily at 3:00 AM UTC.

CREATE TABLE IF NOT EXISTS public.screenshot_storage_deletion_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    screenshot_id UUID NOT NULL,
    storage_path TEXT NOT NULL,
    enqueued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT,
    UNIQUE (screenshot_id)
);

CREATE INDEX IF NOT EXISTS idx_screenshot_storage_deletion_queue_pending
    ON public.screenshot_storage_deletion_queue (enqueued_at)
    WHERE processed_at IS NULL;

CREATE OR REPLACE FUNCTION public.sweep_expired_screenshots()
RETURNS INTEGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public, pg_temp
AS $$
DECLARE
    deleted_count INT := 0;
BEGIN
    -- Lock a bounded batch so concurrent cron invocations do not process the
    -- same screenshots. The temporary table also scopes all later work to
    -- this invocation instead of using a global MAX(deleted_at).
    CREATE TEMP TABLE IF NOT EXISTS retention_sweep_batch (
        id UUID PRIMARY KEY,
        family_id UUID NOT NULL,
        storage_path TEXT NOT NULL
    ) ON COMMIT DROP;

    TRUNCATE retention_sweep_batch;

    INSERT INTO retention_sweep_batch (id, family_id, storage_path)
    SELECT id, family_id, storage_path
    FROM screenshots
    WHERE retention_expires_at < NOW()
      AND deleted_at IS NULL
    ORDER BY retention_expires_at, id
    LIMIT 1000
    FOR UPDATE SKIP LOCKED;

    -- A trusted worker should claim pending rows and call the Supabase
    -- Storage API with these paths. Do not put a service-role key in SQL.
    INSERT INTO screenshot_storage_deletion_queue (screenshot_id, storage_path)
    SELECT id, storage_path
    FROM retention_sweep_batch
    ON CONFLICT (screenshot_id) DO NOTHING;

    UPDATE screenshots AS s
    SET deleted_at = NOW()
    FROM retention_sweep_batch AS b
    WHERE s.id = b.id
      AND s.deleted_at IS NULL;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    -- Log only families represented in this run's batch.
    INSERT INTO activity_log (family_id, event_type, details, created_at)
    SELECT family_id,
           'RETENTION_SWEEP',
           jsonb_build_object(
               'deleted_count', COUNT(*),
               'storage_paths_queued', COUNT(*),
               'timestamp', NOW()
           ),
           NOW()
    FROM retention_sweep_batch
    GROUP BY family_id;
    
    RAISE NOTICE 'Retention sweep completed: % screenshots deleted', deleted_count;
    RETURN deleted_count;
END;
$$;

-- ============================================================================
-- STORAGE DELETION WORKER CONTRACT
-- ============================================================================
-- The worker should:
--   1. Claim rows where processed_at IS NULL.
--   2. Delete bucket "screenshots" / storage_path through the Supabase Storage
--      API (or another trusted Storage client).
--   3. Set processed_at on success, or increment attempts and set last_error
--      on failure.
-- Keeping this boundary explicit avoids a misleading database-only "delete"
-- that leaves the encrypted object in Storage.

-- ============================================================================
-- SCHEDULE THE CRON JOB
-- ============================================================================
-- Run daily at 3:00 AM UTC

-- Remove all existing jobs with this name so the script is rerunnable.
SELECT cron.unschedule(jobid)
FROM cron.job
WHERE jobname = 'retention-sweep-daily';

-- Schedule the sweep
SELECT cron.schedule(
    'retention-sweep-daily',
    '0 3 * * *',  -- 3:00 AM UTC daily
    'SELECT public.sweep_expired_screenshots();'
);

-- ============================================================================
-- VERIFY SCHEDULE
-- ============================================================================
-- SELECT * FROM cron.job WHERE jobname = 'retention-sweep-daily';

-- ============================================================================
-- MANUAL TEST
-- ============================================================================
-- SELECT sweep_expired_screenshots();

-- ============================================================================
-- CLEANUP ORPHANED STORAGE OBJECTS (Optional)
-- ============================================================================
-- Run weekly to find Storage objects without corresponding DB rows
-- This requires pg_net or external script

-- Orphan cleanup is intentionally not scheduled here. Listing and deleting
-- Storage objects belongs in the same trusted worker that drains the queue.
