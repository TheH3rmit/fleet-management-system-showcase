DO $$
BEGIN
    UPDATE activity_types
    SET name = btrim(name)
    WHERE name IS NOT NULL AND name <> btrim(name);

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_activity_type_name_not_blank'
    ) THEN
        ALTER TABLE activity_types
            ADD CONSTRAINT chk_activity_type_name_not_blank CHECK (
                name IS NOT NULL AND length(btrim(name)) > 0
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_class WHERE relname = 'ux_activity_type_name_ci'
    ) THEN
        CREATE UNIQUE INDEX ux_activity_type_name_ci
            ON activity_types (lower(btrim(name)));
    END IF;
END $$;
