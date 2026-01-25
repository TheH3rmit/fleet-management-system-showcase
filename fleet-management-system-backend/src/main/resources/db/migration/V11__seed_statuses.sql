-- V11__seed_statuses.sql
-- Additional seed data to cover all status variants.

-- === USERS (extra) ===
INSERT INTO users (first_name, last_name, email, phone, birth_date)
VALUES
    ('Seed', 'Inactive', 'inactive@local', '100-200-310', '1993-01-10'),
    ('Seed', 'Archived', 'archived@local', '100-200-311', '1993-01-11'),
    ('Seed', 'Deleted', 'deleted@local', '100-200-312', '1993-01-12'),
    ('Seed', 'Driver3', 'driver3@local', '100-200-313', '1987-02-10'),
    ('Seed', 'Driver4', 'driver4@local', '100-200-314', '1986-03-11'),
    ('Seed', 'Driver5', 'driver5@local', '100-200-315', '1985-04-12'),
    ('Seed', 'Driver6', 'driver6@local', '100-200-316', '1984-05-13')
ON CONFLICT DO NOTHING;

-- === ACCOUNTS (status coverage) ===
INSERT INTO accounts (user_id, login, password_hash, created_at, status)
VALUES
    ((SELECT id FROM users WHERE email='inactive@local'),
     'inactive',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'INACTIVE'),
    ((SELECT id FROM users WHERE email='archived@local'),
     'archived',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ARCHIVED'),
    ((SELECT id FROM users WHERE email='deleted@local'),
     'deleted',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'DELETED'),
    ((SELECT id FROM users WHERE email='driver3@local'),
     'driver3',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='driver4@local'),
     'driver4',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='driver5@local'),
     'driver5',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),
    ((SELECT id FROM users WHERE email='driver6@local'),
     'driver6',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE')
ON CONFLICT DO NOTHING;

-- === ACCOUNT ROLES ===
INSERT INTO account_roles (account_id, role)
VALUES
    ((SELECT id FROM accounts WHERE login='inactive'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login='archived'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login='deleted'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login='driver3'), 'DRIVER'),
    ((SELECT id FROM accounts WHERE login='driver4'), 'DRIVER'),
    ((SELECT id FROM accounts WHERE login='driver5'), 'DRIVER'),
    ((SELECT id FROM accounts WHERE login='driver6'), 'DRIVER')
ON CONFLICT DO NOTHING;

-- === DRIVERS (status coverage) ===
INSERT INTO drivers (user_id, driver_license_number, driver_license_category, driver_license_expiry_date, driver_status)
VALUES
    ((SELECT id FROM users WHERE email='driver3@local'), '345678', 'C+E', '2032-12-31', 'AVAILABLE'),
    ((SELECT id FROM users WHERE email='driver4@local'), '456789', 'C+E', '2033-06-30', 'ON_TRANSPORT'),
    ((SELECT id FROM users WHERE email='driver5@local'), '567890', 'C',   '2034-01-15', 'UNAVAILABLE'),
    ((SELECT id FROM users WHERE email='driver6@local'), '678901', 'C',   '2035-03-20', 'SUSPENDED')
ON CONFLICT (user_id) DO NOTHING;

-- === VEHICLES (status coverage) ===
INSERT INTO vehicles (manufacturer, model, date_of_production, mileage, fuel_type, vehicle_status,
                      license_plate, allowed_load, insurance_number)
VALUES
    ('Seed', 'Truck A1', '2018-01-01', 100000, 'Diesel', 'ACTIVE', 'SEED-A1', 24000, '600001'),
    ('Seed', 'Truck A2', '2018-02-01', 110000, 'Diesel', 'ACTIVE', 'SEED-A2', 24000, '600002'),
    ('Seed', 'Truck A3', '2018-03-01', 120000, 'Diesel', 'ACTIVE', 'SEED-A3', 24000, '600003'),
    ('Seed', 'Truck A4', '2018-04-01', 130000, 'Diesel', 'ACTIVE', 'SEED-A4', 24000, '600004'),
    ('Seed', 'Truck A5', '2018-05-01', 140000, 'Diesel', 'ACTIVE', 'SEED-A5', 24000, '600005'),
    ('Seed', 'Truck A6', '2018-06-01', 150000, 'Diesel', 'ACTIVE', 'SEED-A6', 24000, '600006'),
    ('Seed', 'Truck Inactive', '2017-01-01', 160000, 'Diesel', 'INACTIVE', 'SEED-INACT', 20000, '600007'),
    ('Seed', 'Truck Broken', '2017-02-01', 170000, 'Diesel', 'BROKEN', 'SEED-BROKE', 20000, '600008'),
    ('Seed', 'Truck Service', '2017-03-01', 180000, 'Diesel', 'IN_SERVICE', 'SEED-SERV', 20000, '600009')
ON CONFLICT DO NOTHING;

-- === TRAILERS (status coverage) ===
INSERT INTO trailers (name, license_plate, payload, volume, trailer_status)
VALUES
    ('Seed Trailer A1', 'SEED-T1', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer A2', 'SEED-T2', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer A3', 'SEED-T3', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer A4', 'SEED-T4', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer A5', 'SEED-T5', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer A6', 'SEED-T6', 26000.00, 90.0, 'ACTIVE'),
    ('Seed Trailer Inactive', 'SEED-TINACT', 24000.00, 85.0, 'INACTIVE'),
    ('Seed Trailer Broken', 'SEED-TBROKE', 24000.00, 85.0, 'BROKEN'),
    ('Seed Trailer Service', 'SEED-TSERV', 24000.00, 85.0, 'IN_SERVICE')
ON CONFLICT DO NOTHING;

-- === LOCATIONS (extra) ===
INSERT INTO locations (street, city, country, postcode, building_number, latitude, longitude)
VALUES
    ('Seed Street A', 'Warsaw', 'PL', '00-010', '1', 52.231000, 21.000100),
    ('Seed Street B', 'Warsaw', 'PL', '00-011', '2', 52.232000, 21.000200),
    ('Seed Street C', 'Gdansk', 'PL', '80-010', '3', 54.352100, 18.646500),
    ('Seed Street D', 'Poznan', 'PL', '60-010', '4', 52.406400, 16.925200),
    ('Seed Street E', 'Wroclaw', 'PL', '50-010', '5', 51.107900, 17.038500),
    ('Seed Street F', 'Lodz',   'PL', '90-010', '6', 51.759300, 19.455900)
ON CONFLICT DO NOTHING;

-- === TRANSPORTS (status coverage) ===
INSERT INTO transports (vehicle_id, driver_id, pickup_address_id, delivery_address_id,
                        status, created_by, trailer_id,
                        contractual_due_at, planned_start_at, planned_end_at,
                        actual_start_at, actual_end_at,
                        planned_distance_km, actual_distance_km)
VALUES
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A1'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver3@local')),
        (SELECT id FROM locations WHERE street='Seed Street A'),
        (SELECT id FROM locations WHERE street='Seed Street B'),
        'ACCEPTED',
        (SELECT id FROM users WHERE email='dispatch@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T1'),
        NOW()+INTERVAL '5 days',
        NOW()+INTERVAL '1 day',
        NOW()+INTERVAL '2 days',
        NULL, NULL,
        220.0, NULL
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A2'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver4@local')),
        (SELECT id FROM locations WHERE street='Seed Street B'),
        (SELECT id FROM locations WHERE street='Seed Street C'),
        'IN_PROGRESS',
        (SELECT id FROM users WHERE email='dispatch@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T2'),
        NOW()+INTERVAL '2 days',
        NOW()-INTERVAL '3 hours',
        NOW()+INTERVAL '8 hours',
        NOW()-INTERVAL '3 hours',
        NULL,
        180.0, NULL
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A3'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver5@local')),
        (SELECT id FROM locations WHERE street='Seed Street C'),
        (SELECT id FROM locations WHERE street='Seed Street D'),
        'FINISHED',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T3'),
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '1 day',
        NOW()-INTERVAL '2 days',
        NOW()-INTERVAL '1 day',
        300.0, 295.0
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A4'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver6@local')),
        (SELECT id FROM locations WHERE street='Seed Street D'),
        (SELECT id FROM locations WHERE street='Seed Street E'),
        'CANCELLED',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T4'),
        NOW()+INTERVAL '4 days',
        NOW()+INTERVAL '2 days',
        NOW()+INTERVAL '3 days',
        NOW()+INTERVAL '2 days',
        NOW()+INTERVAL '2 days' + INTERVAL '1 hour',
        150.0, NULL
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A5'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver3@local')),
        (SELECT id FROM locations WHERE street='Seed Street E'),
        (SELECT id FROM locations WHERE street='Seed Street F'),
        'FAILED',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T5'),
        NOW()+INTERVAL '6 days',
        NOW()+INTERVAL '3 days',
        NOW()+INTERVAL '4 days',
        NOW()+INTERVAL '3 days',
        NOW()+INTERVAL '3 days' + INTERVAL '2 hours',
        260.0, NULL
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='SEED-A6'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver4@local')),
        (SELECT id FROM locations WHERE street='Seed Street F'),
        (SELECT id FROM locations WHERE street='Seed Street A'),
        'REJECTED',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers WHERE license_plate='SEED-T6'),
        NOW()+INTERVAL '7 days',
        NOW()+INTERVAL '4 days',
        NOW()+INTERVAL '5 days',
        NULL, NULL,
        210.0, NULL
    );

-- === CARGOS (for new transports) ===
INSERT INTO cargos (cargo_description, weight_kg, volume_m3, pickup_date, delivery_date, transport_id)
VALUES
    ('Seed cargo accepted', 5000.00, 20.0, NOW()+INTERVAL '1 day', NOW()+INTERVAL '2 days',
     (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A1'))),
    ('Seed cargo in progress', 6000.00, 25.0, NOW()-INTERVAL '2 hours', NOW()+INTERVAL '6 hours',
     (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A2'))),
    ('Seed cargo finished', 7000.00, 30.0, NOW()-INTERVAL '2 days', NOW()-INTERVAL '1 day',
     (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A3')));

-- === STATUS HISTORIES (for new transports) ===
INSERT INTO status_histories (transport_id, status, changed_at, changed_by)
VALUES
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A1')),
        'ACCEPTED',
        NOW()-INTERVAL '6 hours',
        (SELECT id FROM users WHERE email='driver3@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A2')),
        'IN_PROGRESS',
        NOW()-INTERVAL '2 hours',
        (SELECT id FROM users WHERE email='driver4@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A3')),
        'FINISHED',
        NOW()-INTERVAL '1 day',
        (SELECT id FROM users WHERE email='driver5@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A4')),
        'CANCELLED',
        NOW()-INTERVAL '1 hour',
        (SELECT id FROM users WHERE email='admin@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A5')),
        'FAILED',
        NOW()-INTERVAL '3 hours',
        (SELECT id FROM users WHERE email='admin@local')
    ),
    (
        (SELECT id FROM transports WHERE vehicle_id=(SELECT id FROM vehicles WHERE license_plate='SEED-A6')),
        'REJECTED',
        NOW()-INTERVAL '4 hours',
        (SELECT id FROM users WHERE email='admin@local')
    );
