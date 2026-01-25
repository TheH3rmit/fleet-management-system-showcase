-- Enforce positive numeric constraints for trailers and cargos.

ALTER TABLE trailers
    ALTER COLUMN payload SET NOT NULL,
    ALTER COLUMN volume SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_trailer_payload_positive'
    ) THEN
        ALTER TABLE trailers
            ADD CONSTRAINT chk_trailer_payload_positive CHECK (payload > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_trailer_volume_positive'
    ) THEN
        ALTER TABLE trailers
            ADD CONSTRAINT chk_trailer_volume_positive CHECK (volume > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_cargo_weight_positive'
    ) THEN
        ALTER TABLE cargos
            ADD CONSTRAINT chk_cargo_weight_positive CHECK (weight_kg > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_cargo_volume_positive'
    ) THEN
        ALTER TABLE cargos
            ADD CONSTRAINT chk_cargo_volume_positive CHECK (volume_m3 > 0);
    END IF;
END $$;
