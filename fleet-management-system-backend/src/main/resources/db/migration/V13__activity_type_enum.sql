-- Convert activity types from lookup table to enum-like string column.

ALTER TABLE driver_work_logs
    ADD COLUMN activity_type VARCHAR(32);

UPDATE driver_work_logs dwl
SET activity_type = at.name
FROM activity_types at
WHERE dwl.activity_type_id = at.id;

UPDATE driver_work_logs
SET activity_type = 'DRIVING'
WHERE activity_type IS NULL;

ALTER TABLE driver_work_logs
    ALTER COLUMN activity_type SET NOT NULL;

ALTER TABLE driver_work_logs
    DROP CONSTRAINT IF EXISTS fk_driver_work_logs_activity_type,
    DROP CONSTRAINT IF EXISTS fk_dwl_activity,
    DROP COLUMN IF EXISTS activity_type_id;

DROP TABLE IF EXISTS activity_types;
