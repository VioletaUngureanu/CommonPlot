-- ============================================================
--  V4__add_auth_tables.sql — Flyway migration
--  Bronze/Silver: challenge-response + password reset tokens
-- ============================================================

-- ── Challenge nonces pentru 3-way authentication ─────────────
CREATE TABLE auth_challenges (
                                 id         SERIAL PRIMARY KEY,
                                 username   VARCHAR(50)  NOT NULL,
                                 nonce      VARCHAR(100) NOT NULL UNIQUE,
                                 created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                                 expires_at TIMESTAMP    NOT NULL,
                                 used       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_challenges_username ON auth_challenges(username);
CREATE INDEX idx_challenges_nonce    ON auth_challenges(nonce);

-- ── Password reset tokens ─────────────────────────────────────
CREATE TABLE password_reset_tokens (
                                       id         SERIAL PRIMARY KEY,
                                       user_id    INTEGER      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token      VARCHAR(100) NOT NULL UNIQUE,
                                       created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                                       expires_at TIMESTAMP    NOT NULL,
                                       used       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_reset_token   ON password_reset_tokens(token);
CREATE INDEX idx_reset_user_id ON password_reset_tokens(user_id);