-- V2__seed_dev_data.sql

-- === USERS ===
INSERT INTO users (first_name, last_name, email, phone, birth_date)
VALUES ('System', 'Admin', 'admin@local', '100-200-300', '1990-01-01'),
       ('Daria',  'Dispatcher', 'dispatch@local', '100-200-301', '1992-05-02'),
       ('Jan',    'Kowalski', 'driver1@local', '100-200-302', '1988-09-09'),
       ('Anna',   'Nowak',    'driver2@local', '100-200-303', '1991-03-15')
ON CONFLICT DO NOTHING;

-- === ACCOUNTS (roles -> TEXT[]) ===
INSERT INTO accounts (user_id, login, password_hash, created_at, status)
VALUES
    ((SELECT id FROM users WHERE email='admin@local'),
     'admin',
     '$2a$10$8QyO83kvLBV8xCncl8/CDePEKE75svrgYnQmST1Gmm9I67ury3KJa',
     NOW(), 'ACTIVE'),

    ((SELECT id FROM users WHERE email='dispatch@local'),
     'dispatcher',
     '$2a$10$Snj9xhcv0F1gGrKZ9bL1wufWMKI9Ve.ZTo74w/vUEYJQPhVeqwm7q',
     NOW(), 'ACTIVE'),

    ((SELECT id FROM users WHERE email='driver1@local'),
     'driver1',
     '$2a$10$AJw2QdhBUZRWY3LnYS5/ieMxDgh0/yNHgld.0BGfNWB9U8ZcFU84.',
     NOW(), 'ACTIVE'),

    ((SELECT id FROM users WHERE email='driver2@local'),
     'driver2',
     '$2a$10$bqFtE26bI/6mUAt.hP/vxOBwhxRArfnAsNv/pidnkl4P8MsvgIFcq',
     NOW(), 'ACTIVE')
ON CONFLICT DO NOTHING;

-- === ACCOUNT ROLES ===
INSERT INTO account_roles (account_id, role)
VALUES
    ((SELECT id FROM accounts WHERE login = 'admin'),      'ADMIN'),
    ((SELECT id FROM accounts WHERE login = 'admin'),      'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login = 'dispatcher'), 'DISPATCHER'),
    ((SELECT id FROM accounts WHERE login = 'driver1'),    'DRIVER'),
    ((SELECT id FROM accounts WHERE login = 'driver2'),    'DRIVER')
ON CONFLICT DO NOTHING;

-- === DRIVERS ===
INSERT INTO drivers (user_id, driver_license_number, driver_license_category, driver_license_expiry_date)
VALUES
    ((SELECT id FROM users WHERE email='driver1@local'), '123456', 'C+E', '2030-12-31'),
    ((SELECT id FROM users WHERE email='driver2@local'), '234567', 'C',  '2031-06-30')
ON CONFLICT DO NOTHING;

-- === VEHICLES ===
INSERT INTO vehicles (manufacturer, model, date_of_production, mileage, fuel_type, vehicle_status,
                      license_plate, allowed_load, insurance_number)
VALUES ('Volvo', 'FH16', '2019-03-01', 320000, 'Diesel', 'ACTIVE', 'WX12345', 24000, '555001'),
       ('Scania', 'R450', '2020-07-01', 210000, 'Diesel', 'ACTIVE', 'WX54321', 24000, '555002')
ON CONFLICT DO NOTHING;

-- === TRAILERS ===
INSERT INTO trailers (name, license_plate, payload, volume, trailer_status)
VALUES ('Test1', 'ABC-111',26000.00, 90.0,  'ACTIVE'),
       ('Test2', 'ABC-112',24000.00, 85.5,  'ACTIVE')
ON CONFLICT DO NOTHING;


-- === LOCATIONS ===
INSERT INTO locations (street, city, country, postcode, building_number, latitude,longitude)
VALUES ('Magazynowa','Warszawa','PL','00-001','10',52.229700,52.229700),
       ('Przemysłowa','Łódź','PL','90-001','5',51.759200,51.759200),
       ('Portowa','Gdańsk','PL','80-001','3',54.352000,54.352000)
ON CONFLICT DO NOTHING;

-- === TRANSPORTS (status = ENUM string) ===
INSERT INTO transports (vehicle_id, driver_id, pickup_address_id, delivery_address_id,
                        status, created_by, trailer_id,
                        contractual_due_at, planned_start_at, planned_end_at,
                        actual_start_at, actual_end_at,
                        planned_distance_km, actual_distance_km)
VALUES
    (
        (SELECT id FROM vehicles WHERE license_plate='WX12345'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver1@local')),
        (SELECT id FROM locations WHERE street='Magazynowa'),
        (SELECT id FROM locations WHERE street='Portowa'),
        'PLANNED',
        (SELECT id FROM users WHERE email='admin@local'),
        (SELECT id FROM trailers LIMIT 1),
        NOW()+INTERVAL '7 days',
        NOW()+INTERVAL '1 day',
        NOW()+INTERVAL '2 days',
        NULL, NULL,
        340.0, NULL
    ),
    (
        (SELECT id FROM vehicles WHERE license_plate='WX54321'),
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver2@local')),
        (SELECT id FROM locations WHERE street='Przemysłowa'),
        (SELECT id FROM locations WHERE street='Magazynowa'),
        'IN_PROGRESS',
        (SELECT id FROM users WHERE email='dispatch@local'),
        (SELECT id FROM trailers OFFSET 1 LIMIT 1),
        NOW()+INTERVAL '3 days',
        NOW()-INTERVAL '2 hours',
        NOW()+INTERVAL '10 hours',
        NOW()-INTERVAL '2 hours',
        NULL,
        130.0, NULL
    );

-- === CARGOS ===
INSERT INTO cargos (cargo_description, weight_kg, volume_m3, pickup_date, delivery_date, transport_id)
VALUES
    ('Palletized food', 12000.00, 45.0, NOW()+INTERVAL '1 day', NOW()+INTERVAL '2 days',
     (SELECT id FROM transports LIMIT 1)),
    ('Electronics', 8000.00, 30.0, NOW()-INTERVAL '1 hour', NOW()+INTERVAL '10 hours',
     (SELECT id FROM transports OFFSET 1 LIMIT 1));

-- === ACTIVITY TYPES ===
INSERT INTO activity_types (name)
VALUES ('DRIVING'), ('LOADING'), ('UNLOADING'), ('BREAK')
ON CONFLICT DO NOTHING;

-- === DRIVER WORK LOGS ===
INSERT INTO driver_work_logs (driver_id, transport_id, start_time, end_time, break_duration, notes, activity_type_id)
VALUES
    (
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver1@local')),
        (SELECT id FROM transports LIMIT 1),
        NOW()+INTERVAL '1 day',
        NOW()+INTERVAL '1 day'+'4 hours',
        30, 'Day 1 driving',
        (SELECT id FROM activity_types WHERE name='DRIVING')
    ),
    (
        (SELECT user_id FROM drivers WHERE user_id=(SELECT id FROM users WHERE email='driver2@local')),
        (SELECT id FROM transports OFFSET 1 LIMIT 1),
        NOW()-INTERVAL '2 hours',
        NOW()+INTERVAL '4 hours',
        15, 'On route',
        (SELECT id FROM activity_types WHERE name='DRIVING')
    );

-- === LOGIN HISTORY ===
INSERT INTO login_histories (account_id, logged_at, ip, user_agent, result)
VALUES
    ((SELECT id FROM accounts WHERE login='admin'), CURRENT_DATE, '2130706433', '1', 'SUCCESS'),
    ((SELECT id FROM accounts WHERE login='dispatcher'), CURRENT_DATE, '2130706433', '1', 'SUCCESS'),
    ((SELECT id FROM accounts WHERE login='driver1'), CURRENT_DATE, '2130706433', '1', 'SUCCESS'),
    ((SELECT id FROM accounts WHERE login='driver2'), CURRENT_DATE, '2130706433', '1', 'FAIL');

-- === STATUS HISTORIES (seed) ===
INSERT INTO status_histories (transport_id, status, changed_at, changed_by)
VALUES
    (
        (SELECT id FROM transports ORDER BY id ASC LIMIT 1),
        'PLANNED',
        NOW() - INTERVAL '2 days',
        (SELECT id FROM users WHERE email='admin@local')
    ),
    (
        (SELECT id FROM transports ORDER BY id ASC LIMIT 1),
        'ACCEPTED',
        NOW() - INTERVAL '1 day',
        (SELECT id FROM users WHERE email='driver1@local')
    ),
    (
        (SELECT id FROM transports ORDER BY id ASC OFFSET 1 LIMIT 1),
        'PLANNED',
        NOW() - INTERVAL '1 day',
        (SELECT id FROM users WHERE email='dispatch@local')
    ),
    (
        (SELECT id FROM transports ORDER BY id ASC OFFSET 1 LIMIT 1),
        'IN_PROGRESS',
        NOW() - INTERVAL '2 hours',
        (SELECT id FROM users WHERE email='driver2@local')
    );
