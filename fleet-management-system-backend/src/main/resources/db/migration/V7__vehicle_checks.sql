DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vehicle_mileage_non_negative'
    ) THEN
        ALTER TABLE vehicles
            ADD CONSTRAINT chk_vehicle_mileage_non_negative CHECK (
                mileage IS NULL OR mileage >= 0
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_vehicle_allowed_load_non_negative'
    ) THEN
        ALTER TABLE vehicles
            ADD CONSTRAINT chk_vehicle_allowed_load_non_negative CHECK (
                allowed_load IS NULL OR allowed_load >= 0
            );
    END IF;
END $$;
