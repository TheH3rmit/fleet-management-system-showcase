-- V12__seed_scenarios.sql
-- Seed data for business scenarios and full transport lifecycle.

-- === USERS ===
INSERT INTO users (first_name, last_name, email, phone, birth_date)
VALUES
    ('Seed', 'Admin2', 'admin2@local', '100-200-320', '1989-01-01'),
    ('Seed', 'Dispatcher2', 'dispatch2@local', '100-200-321', '1990-02-02'),
    ('Seed', 'Driver7', 'driver7@local', '100-200-322', '1985-03-03'),
    ('Seed', 'Driver8', 'driver8@local', '100-200-323', '1986-04-04')
ON CONFLICT DO NOTHING;

-- === ACCOUNTS ===
INSERT INTO accounts (user_id, login, password_hash, created_at, status)
VALUES
    ((SELECT id FROM users WHERE email='admin2@local'),
     'admin2',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='dispatch2@local'),
     'dispatcher2',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='driver7@local'),
     'driver7',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='driver8@local'),
     'driver8',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE')
ON CONFLICT DO NOTHING;

-- === ACCOUNT ROLES ===
INSERT INTO account_roles (account_id, role)
VALUES
    ((SELECT id FROM accounts WHERE login='admin2'), 'ADMIN'),
    ((SELECT id FROM accounts WHERE login='admin2'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login='dispatcher2'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login='driver7'), 'DRIVER'),
    ((SELECT id FROM accounts WHERE login='driver8'), 'DRIVER')
ON CONFLICT DO NOTHING;

-- === DRIVERS ===
INSERT INTO drivers (user_id, driver_license_number, driver_license_category, driver_license_expiry_date, driver_status)
VALUES
    ((SELECT id FROM users WHERE email='driver7@local'), '778899', 'C+E', '2036-12-31', 'AVAILABLE'),
    ((SELECT id FROM users WHERE email='driver8@local'), '889900', 'C',   '2037-06-30', 'AVAILABLE')
ON CONFLICT (user_id) DO NOTHING;

-- === VEHICLES ===
INSERT INTO vehicles (manufacturer, model, date_of_production, mileage, fuel_type, vehicle_status,
                      license_plate, allowed_load, insurance_number)
VALUES
    ('Seed', 'CycleTruck1', '2019-01-01', 90000, 'Diesel', 'ACTIVE', 'CYC-001', 24000, '700001'),
    ('Seed', 'CycleTruck2', '2019-02-01', 91000, 'Diesel', 'ACTIVE', 'CYC-002', 24000, '700002'),
    ('Seed', 'BlockTruck1', '2019-03-01', 92000, 'Diesel', 'ACTIVE', 'BLK-001', 24000, '700003')
ON CONFLICT DO NOTHING;

-- === TRAILERS ===
INSERT INTO trailers (name, license_plate, payload, volume, trailer_status)
VALUES
    ('CycleTrailer1', 'CYC-T1', 26000.00, 90.0, 'ACTIVE'),
    ('CycleTrailer2', 'CYC-T2', 26000.00, 90.0, 'ACTIVE'),
    ('BlockTrailer1', 'BLK-T1', 24000.00, 85.0, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- === LOCATIONS ===
INSERT INTO locations (street, city, country, postcode, building_number, latitude, longitude)
VALUES
    ('Seed Cycle A', 'Krakow', 'PL', '30-001', '1', 50.064700, 19.945000),
    ('Seed Cycle B', 'Krakow', 'PL', '30-002', '2', 50.065700, 19.946000),
    ('Seed Block A', 'Katowice', 'PL', '40-001', '3', 50.264900, 19.023800),
    ('Seed Block B', 'Katowice', 'PL', '40-002', '4', 50.265900, 19.024800)
ON CONFLICT DO NOTHING;

-- === TRANSPORTS (full cycle + blocking cases) ===
INSERT INTO transports (vehicle_id, driver_id, pickup_address_id, delivery_address_id,
                        status, created_by, trailer_id,
                        contractual_due_at, planned_start_at, planned_end_at,
                        actual_start_at, actual_end_at,
                        planned_distance_km, actual_distance_km)
VALUES
    -- Full lifecycle transport (FINISHED)
    (
        (SELECT id FROM vehicles WHERE license_plate='CYC-001'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver7@local')),
        (SELECT id FROM locations WHERE street='Seed Cycle A'),
        (SELECT id FROM locations WHERE street='Seed Cycle B'),
        'FINISHED',
        (SELECT id FROM users WHERE email='dispatch2@local'),
        (SELECT id FROM trailers WHERE license_plate='CYC-T1'),
        NOW()-INTERVAL '1 day',
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '1 day',
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '1 day',
        320.0, 315.0
    ),
    -- Planned transport without driver (allowed)
    (
        (SELECT id FROM vehicles WHERE license_plate='CYC-002'),
        NULL,
        (SELECT id FROM locations WHERE street='Seed Cycle B'),
        (SELECT id FROM locations WHERE street='Seed Cycle A'),
        'PLANNED',
        (SELECT id FROM users WHERE email='dispatch2@local'),
        (SELECT id FROM trailers WHERE license_plate='CYC-T2'),
        NOW()+INTERVAL '5 days',
        NOW()+INTERVAL '2 days',
        NOW()+INTERVAL '3 days',
        NULL, NULL,
        200.0, NULL
    ),
    -- In progress transport used for block checks
    (
        (SELECT id FROM vehicles WHERE license_plate='BLK-001'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver8@local')),
        (SELECT id FROM locations WHERE street='Seed Block A'),
        (SELECT id FROM locations WHERE street='Seed Block B'),
        'IN_PROGRESS',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers WHERE license_plate='BLK-T1'),
        NOW()+INTERVAL '2 days',
        NOW()-INTERVAL '4 hours',
        NOW()+INTERVAL '10 hours',
        NOW()-INTERVAL '4 hours',
        NULL,
        140.0, NULL
    );

-- === CARGOS (for transports with trailers) ===
INSERT INTO cargos (cargo_description, weight_kg, volume_m3, pickup_date, delivery_date, transport_id)
VALUES
    ('Cycle cargo', 4000.00, 18.0, NOW()-INTERVAL '2 days', NOW()-INTERVAL '1 day',
     (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001'))),
    ('Block cargo', 5000.00, 20.0, NOW()-INTERVAL '3 hours', NOW()+INTERVAL '6 hours',
     (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='BLK-001')));

-- === STATUS HISTORIES (full cycle timeline) ===
INSERT INTO status_histories (transport_id, status, changed_at, changed_by)
VALUES
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        'PLANNED',
        NOW()-INTERVAL '3 days',
        (SELECT id FROM users WHERE email='dispatch2@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        'ACCEPTED',
        NOW()-INTERVAL '2 days' + INTERVAL '2 hours',
        (SELECT id FROM users WHERE email='driver7@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        'IN_PROGRESS',
        NOW()-INTERVAL '2 days',
        (SELECT id FROM users WHERE email='driver7@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        'FINISHED',
        NOW()-INTERVAL '1 day',
        (SELECT id FROM users WHERE email='driver7@local')
    );

-- === DRIVER WORK LOGS (full cycle transport) ===
INSERT INTO driver_work_logs (driver_id, transport_id, start_time, end_time, break_duration, notes, activity_type_id)
VALUES
    (
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver7@local')),
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '2 days' + INTERVAL '3 hours',
        20, 'Cycle day 1 driving',
        (SELECT id FROM activity_types WHERE name='DRIVING')
    ),
    (
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver7@local')),
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='CYC-001')),
        NOW()-INTERVAL '2 days' + INTERVAL '4 hours',
        NOW()-INTERVAL '2 days' + INTERVAL '6 hours',
        15, 'Cycle loading',
        (SELECT id FROM activity_types WHERE name='LOADING')
    );
