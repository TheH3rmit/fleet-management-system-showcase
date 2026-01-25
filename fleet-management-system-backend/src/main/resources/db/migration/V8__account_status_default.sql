DO $$
BEGIN
    UPDATE accounts
    SET status = 'ACTIVE'
    WHERE status IS NULL;

    ALTER TABLE accounts
        ALTER COLUMN status SET DEFAULT 'ACTIVE',
        ALTER COLUMN status SET NOT NULL;
END $$;
