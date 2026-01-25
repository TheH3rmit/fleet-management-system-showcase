ALTER TABLE login_histories
    ALTER COLUMN logged_at TYPE TIMESTAMP
    USING logged_at::timestamp;
