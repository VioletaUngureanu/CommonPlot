
CREATE TABLE action_logs (
                             id               BIGSERIAL PRIMARY KEY,
                             user_id          INTEGER      REFERENCES users(id) ON DELETE SET NULL,
                             username         VARCHAR(50)  NOT NULL,
                             group_id         VARCHAR(20)  NOT NULL,  -- 'ADMIN' | 'USER'
                             action_type      VARCHAR(50)  NOT NULL,  -- 'LOGIN_FAILED', 'CREATE_MEETUP', etc.
                             action_info      TEXT,                   -- detalii extra (ex: endpoint, entitate)
                             ip_address       VARCHAR(50),
                             timestamp        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logs_user_id    ON action_logs(user_id);
CREATE INDEX idx_logs_username   ON action_logs(username);
CREATE INDEX idx_logs_action     ON action_logs(action_type);
CREATE INDEX idx_logs_timestamp  ON action_logs(timestamp);


CREATE TABLE suspicious_users (
                                  id               SERIAL PRIMARY KEY,
                                  user_id          INTEGER      REFERENCES users(id) ON DELETE CASCADE,
                                  username         VARCHAR(50)  NOT NULL,
                                  reason           TEXT         NOT NULL,   -- descrierea comportamentului
                                  detected_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                                  resolved         BOOLEAN      NOT NULL DEFAULT FALSE,
                                  resolved_at      TIMESTAMP,
                                  resolved_by      VARCHAR(50)             -- adminul care a rezolvat
);

CREATE INDEX idx_suspicious_user_id  ON suspicious_users(user_id);
CREATE INDEX idx_suspicious_resolved ON suspicious_users(resolved);