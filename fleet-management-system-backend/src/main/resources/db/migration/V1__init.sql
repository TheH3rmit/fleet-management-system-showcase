-- V1__init.sql
-- === USERS / ACCOUNTS / ROLES ===
-- ========== USERS ==========
CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    phone       VARCHAR(25),
    birth_date  DATE
);
CREATE INDEX ix_user_last_name ON users (last_name);

-- ========== ACCOUNTS ==========
-- roles = TEXT[]     ← enum names (ADMIN, DISPATCHER, DRIVER)
CREATE TABLE accounts
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    login         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP,
    last_login_at TIMESTAMP,
    status        VARCHAR(50),

    CONSTRAINT fk_account_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX ix_account_status ON accounts (status);

-- New table for roles (ElementCollection)
CREATE TABLE account_roles
(
    account_id BIGINT      NOT NULL,
    role       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_account_roles_account
        FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
    CONSTRAINT pk_account_roles PRIMARY KEY (account_id, role)
);

-- ========== LOGIN HISTORY ==========
CREATE TABLE login_histories
(
    id         BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    logged_at  DATE,
    ip         VARCHAR(255),
    user_agent VARCHAR(255),
    result     VARCHAR(20),

    CONSTRAINT fk_login_history_account
        FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);
CREATE INDEX ix_login_history_account ON login_histories (account_id, logged_at);

-- ========== DRIVERS ==========
CREATE TABLE drivers
(
    user_id                    BIGINT PRIMARY KEY,
    driver_license_number      VARCHAR(50),
    driver_license_category    VARCHAR(50),
    driver_license_expiry_date DATE,
    driver_status              VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',

    CONSTRAINT fk_driver_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- ========== VEHICLES ==========
CREATE TABLE vehicles
(
    id                 BIGSERIAL PRIMARY KEY,
    manufacturer       VARCHAR(255),
    model              VARCHAR(255),
    date_of_production DATE,
    mileage            INTEGER,
    fuel_type          VARCHAR(50),
    vehicle_status     VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    license_plate      VARCHAR(255) NOT NULL UNIQUE,
    allowed_load       INTEGER,
    insurance_number   VARCHAR(50)
);

-- ========== TRAILERS ==========
CREATE TABLE trailers
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255),
    license_plate VARCHAR(255),
    payload       NUMERIC(10, 2) NOT NULL,
    volume        NUMERIC(10, 3) NOT NULL,
    trailer_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_trailer_payload_positive CHECK (payload > 0),
    CONSTRAINT chk_trailer_volume_positive CHECK (volume > 0)
);

-- ========== LOCATIONS ==========
CREATE TABLE locations
(
    id                   BIGSERIAL PRIMARY KEY,
    street               VARCHAR(255),
    city                 VARCHAR(255),
    country              VARCHAR(255),
    postcode             VARCHAR(255),
    building_number      VARCHAR(255),
    latitude  NUMERIC(9, 6),
    longitude NUMERIC(9, 6)
);

-- ========== TRANSPORTS ==========
CREATE TABLE transports
(
    id                  BIGSERIAL PRIMARY KEY,
    vehicle_id          BIGINT  NOT NULL,
    driver_id           BIGINT,
    pickup_address_id  BIGINT   NOT NULL,
    delivery_address_id BIGINT  NOT NULL,
    status              VARCHAR(50) NOT NULL, -- enum TransportStatusName
    created_by          BIGINT NOT NULL,
    trailer_id          BIGINT,

    contractual_due_at  TIMESTAMP,
    planned_start_at    TIMESTAMP,
    planned_end_at      TIMESTAMP,
    actual_start_at     TIMESTAMP,
    actual_end_at       TIMESTAMP,
    planned_distance_km NUMERIC(10, 2),
    actual_distance_km  NUMERIC(10, 2),

    CONSTRAINT fk_transport_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),

    CONSTRAINT fk_transport_driver
        FOREIGN KEY (driver_id) REFERENCES drivers (user_id) ON DELETE SET NULL,

    CONSTRAINT fk_transport_pickup
        FOREIGN KEY (pickup_address_id) REFERENCES locations (id),

    CONSTRAINT fk_transport_delivery
        FOREIGN KEY (delivery_address_id) REFERENCES locations (id),

    CONSTRAINT fk_transport_trailer
        FOREIGN KEY (trailer_id) REFERENCES trailers (id) ON DELETE SET NULL,

    CONSTRAINT fk_transport_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX ix_transport_status ON transports (status);
CREATE INDEX ix_transport_driver ON transports (driver_id);
CREATE INDEX ix_transport_vehicle ON transports (vehicle_id);

-- ========== CARGO ==========
CREATE TABLE cargos
(
    id                BIGSERIAL PRIMARY KEY,
    cargo_description VARCHAR(255),
    weight_kg         NUMERIC(10, 2) NOT NULL,
    volume_m3         NUMERIC(10, 3) NOT NULL,
    pickup_date       TIMESTAMP,
    delivery_date     TIMESTAMP,
    transport_id      BIGINT NOT NULL,

    CONSTRAINT fk_cargo_transport
        FOREIGN KEY (transport_id) REFERENCES transports (id) ON DELETE CASCADE,
    CONSTRAINT chk_cargo_weight_positive CHECK (weight_kg > 0),
    CONSTRAINT chk_cargo_volume_positive CHECK (volume_m3 > 0)
);
CREATE INDEX ix_cargo_transport ON cargos (transport_id);

-- ========== ACTIVITY TYPES ==========
CREATE TABLE activity_types
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- ========== DRIVER WORK LOGS ==========
CREATE TABLE driver_work_logs
(
    id               BIGSERIAL PRIMARY KEY,
    driver_id        BIGINT NOT NULL,
    transport_id     BIGINT NOT NULL,
    start_time       TIMESTAMP,
    end_time         TIMESTAMP,
    break_duration   INTEGER,
    notes            VARCHAR(255),
    activity_type_id BIGINT,

    CONSTRAINT fk_dwl_driver
        FOREIGN KEY (driver_id) REFERENCES drivers (user_id) ON DELETE CASCADE,

    CONSTRAINT fk_dwl_transport
        FOREIGN KEY (transport_id) REFERENCES transports (id) ON DELETE SET NULL,

    CONSTRAINT fk_dwl_activity
        FOREIGN KEY (activity_type_id) REFERENCES activity_types (id) ON DELETE SET NULL
);
CREATE INDEX ix_dwl_driver_time ON driver_work_logs (driver_id, start_time);

-- ========= STATUS HISTORIES ==========
CREATE TABLE status_histories
(
    id           BIGSERIAL PRIMARY KEY,
    transport_id BIGINT      NOT NULL,
    status       VARCHAR(50) NOT NULL,
    changed_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    changed_by   BIGINT,

    CONSTRAINT fk_status_history_transport
        FOREIGN KEY (transport_id) REFERENCES transports (id) ON DELETE CASCADE,

    CONSTRAINT fk_status_history_user
        FOREIGN KEY (changed_by) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX ix_status_history_transport
    ON status_histories (transport_id, changed_at);
