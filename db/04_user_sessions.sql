-- Задание 5: серверный учёт выданных пар токенов (access + refresh).
-- Выполняется администратором (postgres) в БД garden ПОСЛЕ 02_schema.sql (нужна таблица users):
--   sudo -u postgres psql -d garden -f db/04_user_sessions.sql
-- Данные остальных таблиц скрипт не трогает. Повторный запуск пересоздаёт только user_sessions.

DROP TABLE IF EXISTS user_sessions;

CREATE TABLE user_sessions (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- генерируемый UUID
    username              VARCHAR(255) NOT NULL                                  -- владелец пары: email пользователя (sub токена)
                          CHECK (length(btrim(username)) > 0)
                          REFERENCES users (email) ON DELETE CASCADE ON UPDATE CASCADE,
    device_id             VARCHAR(128)                                           -- метка клиента, может быть NULL
                          CHECK (device_id IS NULL OR length(btrim(device_id)) > 0),
    access_token          TEXT         NOT NULL,                                 -- выданный access
    refresh_token         TEXT         NOT NULL UNIQUE,                          -- выданный refresh, уникален
    access_token_expiry   TIMESTAMPTZ  NOT NULL,                                 -- совпадает с exp access (до секунды)
    refresh_token_expiry  TIMESTAMPTZ  NOT NULL,                                 -- совпадает с exp refresh (до секунды)
    status                VARCHAR(16)  NOT NULL
                          CHECK (status IN ('ACTIVE', 'USED', 'REVOKED'))        -- ACTIVE / USED / REVOKED
);
CREATE INDEX idx_user_sessions_username ON user_sessions (username);

-- Права сервисной роли: чтение, добавление и изменение строк (без удаления и без изменения структуры).
-- Каскадное удаление сессий при удалении пользователя выполняет сама БД (внешний ключ), права DELETE для этого не нужны.
REVOKE ALL ON user_sessions FROM garden_app;
GRANT SELECT, INSERT, UPDATE ON user_sessions TO garden_app;
