-- ============================================================
--  V2__add_users_roles_permissions.sql — Flyway migration
--  Silver: infrastructură pentru Users, Roles, Permissions
--
--  Schema e în 3NF:
--  - roles: id, name
--  - permissions: id, name
--  - role_permissions: role_id (FK), permission_id (FK)
--  - users: id, username, password, email, role_id (FK)
-- ============================================================

-- ── Roluri ────────────────────────────────────────────────────
CREATE TABLE roles (
                       id   SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- ── Permisiuni ────────────────────────────────────────────────
CREATE TABLE permissions (
                             id   SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE
);

-- ── Relație many-to-many: Role → Permissions ──────────────────
CREATE TABLE role_permissions (
                                  role_id       INTEGER NOT NULL REFERENCES roles(id),
                                  permission_id INTEGER NOT NULL REFERENCES permissions(id),
                                  PRIMARY KEY (role_id, permission_id)
);

-- ── Users ─────────────────────────────────────────────────────
CREATE TABLE users (
                       id         SERIAL PRIMARY KEY,
                       username   VARCHAR(50)  NOT NULL UNIQUE,
                       password   VARCHAR(255) NOT NULL,
                       email      VARCHAR(100) NOT NULL UNIQUE,
                       full_name  VARCHAR(100),
                       role_id    INTEGER NOT NULL REFERENCES roles(id),
                       created_at TIMESTAMP DEFAULT NOW()
);

-- ── Indecși pentru performanță ────────────────────────────────
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_role_id  ON users(role_id);

-- ── Date inițiale: roluri ─────────────────────────────────────
INSERT INTO roles (name) VALUES ('ADMIN'), ('USER');

-- ── Date inițiale: permisiuni ─────────────────────────────────
INSERT INTO permissions (name) VALUES
                                   ('MEETUP_CREATE'),
                                   ('MEETUP_UPDATE'),
                                   ('MEETUP_DELETE'),
                                   ('MEETUP_READ'),
                                   ('BOOK_CREATE'),
                                   ('BOOK_UPDATE'),
                                   ('BOOK_DELETE'),
                                   ('BOOK_READ');

-- ── ADMIN are toate permisiunile ──────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- ── USER are doar READ + CREATE ───────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions
WHERE name IN ('MEETUP_READ', 'MEETUP_CREATE', 'BOOK_READ');

-- ── Useri inițiali ────────────────────────────────────────────
INSERT INTO users (username, password, email, full_name, role_id) VALUES
                                                                      ('admin',  'admin123', 'admin@commonplot.com',  'Admin User',   1),
                                                                      ('user1',  'user123',  'user1@commonplot.com',  'Normal User',  2),
                                                                      ('user2',  'user123',  'user2@commonplot.com',  'Normal User2', 2);