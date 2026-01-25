DO $$
BEGIN
    UPDATE users
    SET email = btrim(email)
    WHERE email IS NOT NULL AND email <> btrim(email);

    UPDATE accounts
    SET login = btrim(login)
    WHERE login IS NOT NULL AND login <> btrim(login);

    IF NOT EXISTS (
        SELECT 1 FROM pg_class WHERE relname = 'ux_users_email_ci'
    ) THEN
        CREATE UNIQUE INDEX ux_users_email_ci
            ON users (lower(btrim(email)));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_class WHERE relname = 'ux_accounts_login_ci'
    ) THEN
        CREATE UNIQUE INDEX ux_accounts_login_ci
            ON accounts (lower(btrim(login)));
    END IF;
END $$;
