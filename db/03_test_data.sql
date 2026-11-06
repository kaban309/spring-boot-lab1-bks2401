
TRUNCATE harvests, plantings, crops, garden_beds, users RESTART IDENTITY CASCADE;

INSERT INTO users (name, email, password_hash, role, status) VALUES
    ('Тимофей',       'timofey@example.com',  '$2a$10$qTe00atAZB8Xs0oEvholJ.zejWO8/2DCyKdK/syv2piNQV1C1cl4y', 'GARDENER',   'ACTIVE'),   -- id 1, пароль Timofey#2026
    ('Анна',          'anna@example.com',     '$2a$10$9sjMW4srufMinaYiFSqRXuHBoJBPbVu4TbK23OdBlVd4O8Z6urQD6', 'GARDENER',   'ACTIVE'),   -- id 2, пароль Anna#2026pw
    ('Администратор', 'admin@example.com',    '$2a$10$PlRr/K/hm9pOv3n0dpczTu2ribss3i4PNTu2MjV6K/U79/hB4SOTq', 'ADMIN',      'ACTIVE'),   -- id 3, пароль Admin#2026pw
    ('Агроном',       'agro@example.com',     '$2a$10$Ka8koRKPkG291f.rWW4xc.JNcwJYVVOe8aEN/bJmp64ukPBjgiW0u', 'AGRONOMIST', 'ACTIVE'),   -- id 4, пароль Agro#2026pw
    ('Заблокирован',  'blocked@example.com',  '$2a$10$f/Re53oRg472V0e7BuodeeroVTLV0kff4M8KB4EZkdFQIqIm4prT6', 'GARDENER',   'BLOCKED');  -- id 5, пароль Blocked#2026 (войти нельзя)

INSERT INTO crops (name, season, min_area_per_plant) VALUES
    ('Томат',  'SPRING', 0.500),
    ('Огурец', 'SUMMER', 0.300),
    ('Чеснок', 'AUTUMN', 0.050),
    ('Зелень', 'WINTER', 0.020);

INSERT INTO garden_beds (area, owner_id, available) VALUES
    (10.00, 1, TRUE),
    ( 4.00, 1, TRUE),
    ( 6.00, 2, FALSE);

INSERT INTO plantings (garden_bed_id, crop_id, plant_count, status) VALUES
    (1, 1, 10, 'GROWING'),
    (1, 3, 20, 'PLANNED'),
    (2, 2,  5, 'GROWING');

INSERT INTO harvests (planting_id, mass) VALUES
    (1, 3.500);
