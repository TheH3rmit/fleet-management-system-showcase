DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_cargo_delivery_after_pickup'
    ) THEN
        ALTER TABLE cargos
            ADD CONSTRAINT chk_cargo_delivery_after_pickup CHECK (
                pickup_date IS NULL
                OR delivery_date IS NULL
                OR delivery_date >= pickup_date
            );
    END IF;
END $$;
