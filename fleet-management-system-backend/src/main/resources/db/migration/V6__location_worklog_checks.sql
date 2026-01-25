DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_location_latitude_range'
    ) THEN
        ALTER TABLE locations
            ADD CONSTRAINT chk_location_latitude_range CHECK (
                latitude IS NULL OR (latitude >= -90 AND latitude <= 90)
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_location_longitude_range'
    ) THEN
        ALTER TABLE locations
            ADD CONSTRAINT chk_location_longitude_range CHECK (
                longitude IS NULL OR (longitude >= -180 AND longitude <= 180)
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_work_log_end_after_start'
    ) THEN
        ALTER TABLE driver_work_logs
            ADD CONSTRAINT chk_work_log_end_after_start CHECK (
                start_time IS NULL
                OR end_time IS NULL
                OR end_time >= start_time
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_work_log_break_positive'
    ) THEN
        ALTER TABLE driver_work_logs
            ADD CONSTRAINT chk_work_log_break_positive CHECK (
                break_duration IS NULL OR break_duration >= 0
            );
    END IF;
END $$;
