
TRUNCATE harvests, plantings, crops, garden_beds, users RESTART IDENTITY CASCADE;

INSERT INTO users (name, email) VALUES
    ('Тимофей', 'timofey@example.com'),
    ('Анна',    'anna@example.com');

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
