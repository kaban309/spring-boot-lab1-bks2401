-- Проверочные запросы к таблице сессий (для шагов «Проверить БД» из задания 5).
-- Запуск:  sudo -u postgres psql -d garden -f db/check_sessions.sql   (или в pgAdmin, Query Tool)

-- Сводка по статусам для сценария из Postman (deviceId = postman-demo):
--   после входа:                    ACTIVE = 1
--   после первого /auth/refresh:    USED = 1, ACTIVE = 1
--   после повтора старого refresh:  REVOKED = 1, ACTIVE = 1 (всего строк 2, новых строк не появилось)
SELECT status, count(*) AS cnt
FROM user_sessions
WHERE username = 'timofey@example.com' AND device_id = 'postman-demo'
GROUP BY status
ORDER BY status;

-- Подробно (токены обрезаны, чтобы не светить их целиком):
SELECT id, username, device_id, status,
       left(refresh_token, 16) || '...'   AS refresh_prefix,
       access_token_expiry, refresh_token_expiry
FROM user_sessions
WHERE username = 'timofey@example.com' AND device_id = 'postman-demo'
ORDER BY refresh_token_expiry, status;

-- Сколько всего сессий и в каких статусах:
SELECT status, count(*) FROM user_sessions GROUP BY status ORDER BY status;
