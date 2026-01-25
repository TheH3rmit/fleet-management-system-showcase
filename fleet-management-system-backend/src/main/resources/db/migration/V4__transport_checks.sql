ALTER TABLE transports
    ADD CONSTRAINT chk_transport_planned_distance_positive
        CHECK (planned_distance_km IS NULL OR planned_distance_km > 0),
    ADD CONSTRAINT chk_transport_actual_distance_positive
        CHECK (actual_distance_km IS NULL OR actual_distance_km > 0),
    ADD CONSTRAINT chk_transport_planned_end_after_start
        CHECK (
            planned_start_at IS NULL
            OR planned_end_at IS NULL
            OR planned_end_at >= planned_start_at
        ),
    ADD CONSTRAINT chk_transport_actual_end_after_start
        CHECK (
            actual_start_at IS NULL
            OR actual_end_at IS NULL
            OR actual_end_at >= actual_start_at
        );
