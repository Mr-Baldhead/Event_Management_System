-- V5: Create global troops and food allergies tables
-- These are shared across all events

-- Troops (Kårer) - global list
CREATE TABLE IF NOT EXISTS troops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_swedish_ci;

-- Food allergies (Matallergier) - global list
CREATE TABLE IF NOT EXISTS food_allergies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_swedish_ci;

-- Insert default troops (Kårer)
INSERT INTO troops (name, sort_order) VALUES
    ('Arvidsjaur', 1),
    ('Furubergskyrkan', 2),
    ('Kustkyrkan', 3),
    ('Luleå', 4),
    ('Norrfjärden', 5),
    ('Rosvik', 6),
    ('Sjulnäs', 7),
    ('Tallhedskyrkan', 8),
    ('Öjebyn', 9);

-- Insert default food allergies (EU 14 allergens + common)
INSERT INTO food_allergies (name, sort_order) VALUES
    ('Selleri', 1),
    ('Spannmål som innehåller gluten (vete, råg, korn, havre)', 2),
    ('Kräftdjur (räkor, krabbor, hummer)', 3),
    ('Ägg', 4),
    ('Fisk', 5),
    ('Lupin', 6),
    ('Mjölk', 7),
    ('Blötdjur (musslor, ostron)', 8),
    ('Senap', 9),
    ('Jordnötter', 10),
    ('Sesam', 11),
    ('Sojabönor', 12),
    ('Svaveldioxid och sulfiter', 13),
    ('Nötter', 14);
