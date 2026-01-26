-- V2__insert_sample_data.sql
-- Sample data for Event Management System
-- Matches V1 schema exactly (no 'status' column)

-- Insert sample events
INSERT INTO events (name, slug, description, start_date, end_date, street_address, postal_code, city, capacity, active) VALUES
                                                                                                                            ('Blåsarläger 2026', 'blasarlager-2026', 'Årligt scoutläger för blåsare och musiker. Kom och spela tillsammans med scouter från hela landet!', '2026-07-15 10:00:00', '2026-07-20 14:00:00', 'Scoutgården Vildmarken 1', '12345', 'Naturby', 150, TRUE),
                                                                                                                            ('Höstvandering 2026', 'hostvandring-2026', 'Vandring i höstfärgerna med övernattning i vindskydd.', '2026-09-20 09:00:00', '2026-09-22 15:00:00', 'Naturreservatet', '54321', 'Skogsby', 50, TRUE),
                                                                                                                            ('Vinterläger 2026', 'vinterlager-2026', 'Vinteraktiviteter och skidåkning för alla åldrar.', '2026-02-10 12:00:00', '2026-02-14 12:00:00', 'Fjällgården', '98765', 'Snöberg', 80, TRUE);

-- Insert sample patrols (kårer) connected to events
INSERT INTO patrols (name, description, contact_person, contact_email, contact_phone, event_id) VALUES
                                                                                                    ('Björnkåren', 'Scouter från Björnstad', 'Anna Björnsson', 'anna@bjornkaren.se', '070-1234567', 1),
                                                                                                    ('Örnpatrullen', 'Scouter från Örnhamn', 'Erik Örnberg', 'erik@ornpatrullen.se', '070-2345678', 1),
                                                                                                    ('Varggruppen', 'Scouter från Vargköping', 'Maria Vargström', 'maria@varggruppen.se', '070-3456789', 1),
                                                                                                    ('Rävscouter', 'Scouter från Rävsta', 'Per Rävlund', 'per@ravscouter.se', '070-4567890', 2);

-- Insert sample allergens
INSERT INTO allergens (name, description, severity, event_id) VALUES
                                                                  ('Gluten', 'Vete, råg, korn och havre', 'HIGH', NULL),
                                                                  ('Laktos', 'Mjölksocker i mejeriprodukter', 'MEDIUM', NULL),
                                                                  ('Nötter', 'Alla typer av nötter', 'CRITICAL', NULL),
                                                                  ('Ägg', 'Hönsägg och äggprodukter', 'HIGH', NULL),
                                                                  ('Soja', 'Sojabönor och sojaprodukter', 'MEDIUM', NULL),
                                                                  ('Fisk', 'Alla typer av fisk', 'HIGH', NULL),
                                                                  ('Skaldjur', 'Räkor, kräftor, musslor etc.', 'CRITICAL', NULL),
                                                                  ('Selleri', 'Selleri och selleriprodukter', 'MEDIUM', NULL),
                                                                  ('Sesam', 'Sesamfrön (används i bröd på lägret)', 'MEDIUM', 1);

-- Insert sample participants
INSERT INTO participants (first_name, last_name, email, phone, birth_date, personal_number, street_address, postal_code, city, guardian_name, guardian_email, guardian_phone, patrol_id) VALUES
                                                                                                                                                                                             ('Emma', 'Andersson', 'emma.andersson@email.se', '073-1111111', '2010-03-15', '20100315-1234', 'Storgatan 1', '12345', 'Stockholm', 'Lars Andersson', 'lars.andersson@email.se', '070-1111111', 1),
                                                                                                                                                                                             ('Oscar', 'Bergström', 'oscar.bergstrom@email.se', '073-2222222', '2008-07-22', '20080722-5678', 'Lillgatan 2', '23456', 'Göteborg', 'Karin Bergström', 'karin.bergstrom@email.se', '070-2222222', 1),
                                                                                                                                                                                             ('Maja', 'Carlsson', 'maja.carlsson@email.se', '073-3333333', '2012-11-30', '20121130-9012', 'Kyrkvägen 3', '34567', 'Malmö', 'Johan Carlsson', 'johan.carlsson@email.se', '070-3333333', 2),
                                                                                                                                                                                             ('William', 'Dahl', 'william.dahl@email.se', '073-4444444', '2005-01-10', '20050110-3456', 'Parkgatan 4', '45678', 'Uppsala', NULL, NULL, NULL, 2),
                                                                                                                                                                                             ('Alice', 'Eriksson', 'alice.eriksson@email.se', '073-5555555', '2009-05-25', '20090525-7890', 'Skogsvägen 5', '56789', 'Västerås', 'Eva Eriksson', 'eva.eriksson@email.se', '070-5555555', 3),
                                                                                                                                                                                             ('Hugo', 'Fransson', 'hugo.fransson@email.se', '073-6666666', '2011-09-08', '20110908-1234', 'Sjövägen 6', '67890', 'Örebro', 'Magnus Fransson', 'magnus.fransson@email.se', '070-6666666', 3),
                                                                                                                                                                                             ('Lisa', 'Gustafsson', 'lisa.gustafsson@email.se', '073-7777777', '2009-02-14', '20090214-5678', 'Bergvägen 7', '78901', 'Linköping', 'Anna Gustafsson', 'anna.gustafsson@email.se', '070-7777777', 4);

-- Connect participants with allergens
INSERT INTO participant_allergens (participant_id, allergen_id) VALUES
                                                                    (1, 1),
                                                                    (1, 2),
                                                                    (2, 3),
                                                                    (3, 4),
                                                                    (5, 1),
                                                                    (5, 5),
                                                                    (6, 7),
                                                                    (7, 1);

-- Insert registrations
INSERT INTO registrations (event_id, participant_id, status, registration_date, confirmation_date, notes) VALUES
                                                                                                              (1, 1, 'CONFIRMED', '2026-01-10 10:00:00', '2026-01-11 09:00:00', 'Kommer med förälder första dagen'),
                                                                                                              (1, 2, 'CONFIRMED', '2026-01-11 14:30:00', '2026-01-12 08:00:00', 'Spelar trumpet'),
                                                                                                              (1, 3, 'CONFIRMED', '2026-01-15 09:15:00', '2026-01-16 10:00:00', NULL),
                                                                                                              (1, 4, 'CONFIRMED', '2026-01-08 16:00:00', '2026-01-09 10:00:00', 'Spelar trombon'),
                                                                                                              (1, 5, 'CONFIRMED', '2026-01-20 11:00:00', '2026-01-21 09:00:00', NULL),
                                                                                                              (1, 6, 'PENDING', '2026-01-22 14:00:00', NULL, 'Väntar på betalning'),
                                                                                                              (2, 7, 'CONFIRMED', '2026-02-01 10:00:00', '2026-02-02 09:00:00', 'Tar med egen tältutrustning'),
                                                                                                              (2, 1, 'CONFIRMED', '2026-02-05 14:00:00', '2026-02-06 08:00:00', NULL);