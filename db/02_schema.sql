
DROP TABLE IF EXISTS harvests, plantings, crops, garden_beds, users CASCADE;

CREATE TABLE users (
    id    BIGSERIAL    PRIMARY KEY,
    name  VARCHAR(100) NOT NULL CHECK (length(btrim(name)) > 0),
    email VARCHAR(255) NOT NULL UNIQUE                      -- один email = один пользователь
);

CREATE TABLE garden_beds (
    id        BIGSERIAL     PRIMARY KEY,
    area      NUMERIC(8, 2) NOT NULL CHECK (area > 0),      -- площадь, м²
    owner_id  BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    available BOOLEAN       NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_garden_beds_owner ON garden_beds (owner_id);

CREATE TABLE crops (
    id                  BIGSERIAL     PRIMARY KEY,
    name                VARCHAR(100)  NOT NULL UNIQUE CHECK (length(btrim(name)) > 0),
    season              VARCHAR(10)   NOT NULL CHECK (season IN ('SPRING', 'SUMMER', 'AUTUMN', 'WINTER')),
    min_area_per_plant  NUMERIC(8, 3) NOT NULL CHECK (min_area_per_plant > 0)   -- м² на одно растение
);

CREATE TABLE plantings (
    id             BIGSERIAL   PRIMARY KEY,
    garden_bed_id  BIGINT      NOT NULL REFERENCES garden_beds (id) ON DELETE CASCADE,
    crop_id        BIGINT      NOT NULL REFERENCES crops (id)       ON DELETE CASCADE,
    plant_count    INTEGER     NOT NULL CHECK (plant_count > 0),
    status         VARCHAR(10) NOT NULL DEFAULT 'PLANNED'
                               CHECK (status IN ('PLANNED', 'GROWING', 'HARVESTED')),
    planted_at     DATE        NOT NULL DEFAULT CURRENT_DATE,
    version        BIGINT      NOT NULL DEFAULT 0            -- оптимистическая блокировка (JPA @Version)
);
CREATE INDEX idx_plantings_bed  ON plantings (garden_bed_id);
CREATE INDEX idx_plantings_crop ON plantings (crop_id);

CREATE TABLE harvests (
    id            BIGSERIAL     PRIMARY KEY,
    planting_id   BIGINT        NOT NULL REFERENCES plantings (id) ON DELETE CASCADE,
    harvest_date  DATE          NOT NULL DEFAULT CURRENT_DATE,
    mass          NUMERIC(10, 3) NOT NULL CHECK (mass > 0)   -- масса, кг
);
CREATE INDEX idx_harvests_planting ON harvests (planting_id);


REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO garden_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO garden_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO garden_app;
