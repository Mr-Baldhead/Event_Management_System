-- V1__create_base_tables.sql
-- Initial database schema for Event Management System

-- Events table
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description VARCHAR(2000),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    street_address VARCHAR(200),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    capacity INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_events_slug (slug),
    INDEX idx_events_start_date (start_date),
    INDEX idx_events_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Patrols (Scout troops / Kårer) table
CREATE TABLE IF NOT EXISTS patrols (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    event_id BIGINT,
    CONSTRAINT fk_patrols_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL,
    INDEX idx_patrols_event (event_id),
    INDEX idx_patrols_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Allergens table
CREATE TABLE IF NOT EXISTS allergens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    event_id BIGINT,
    CONSTRAINT fk_allergens_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    INDEX idx_allergens_event (event_id),
    INDEX idx_allergens_severity (severity),
    INDEX idx_allergens_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Participants table
CREATE TABLE IF NOT EXISTS participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    birth_date DATE,
    personal_number VARCHAR(13),
    street_address VARCHAR(200),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    guardian_name VARCHAR(100),
    guardian_email VARCHAR(100),
    guardian_phone VARCHAR(20),
    patrol_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_participants_patrol FOREIGN KEY (patrol_id) REFERENCES patrols(id) ON DELETE SET NULL,
    INDEX idx_participants_patrol (patrol_id),
    INDEX idx_participants_email (email),
    INDEX idx_participants_name (last_name, first_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Participant allergens junction table
CREATE TABLE IF NOT EXISTS participant_allergens (
    participant_id BIGINT NOT NULL,
    allergen_id BIGINT NOT NULL,
    PRIMARY KEY (participant_id, allergen_id),
    CONSTRAINT fk_pa_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_allergen FOREIGN KEY (allergen_id) REFERENCES allergens(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Registrations table
CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'WAITLIST') NOT NULL DEFAULT 'PENDING',
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmation_date DATETIME,
    cancellation_date DATETIME,
    notes VARCHAR(1000),
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT uk_registration_event_participant UNIQUE (event_id, participant_id),
    INDEX idx_registrations_event (event_id),
    INDEX idx_registrations_participant (participant_id),
    INDEX idx_registrations_status (status),
    INDEX idx_registrations_date (registration_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
