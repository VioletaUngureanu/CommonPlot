CREATE TABLE IF NOT EXISTS books (
                                     id          SERIAL PRIMARY KEY,
                                     title       VARCHAR(200) NOT NULL,
    author      VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    cover_url   VARCHAR(500)
    );

CREATE TABLE IF NOT EXISTS meetups (
                                       id             BIGSERIAL PRIMARY KEY,
                                       title_event    VARCHAR(150) NOT NULL,
    location       VARCHAR(100) NOT NULL,
    date           VARCHAR(50)  NOT NULL,
    book_id        INTEGER      NOT NULL REFERENCES books(id),
    owner_id       INTEGER      NOT NULL,
    owner_username VARCHAR(100) NOT NULL,
    duration       INTEGER      NOT NULL CHECK (duration >= 15 AND duration <= 480),
    rating         DECIMAL(3,1) CHECK (rating >= 0.0 AND rating <= 5.0),
    description    VARCHAR(500)
    );


CREATE INDEX IF NOT EXISTS idx_meetups_book_id   ON meetups(book_id);
CREATE INDEX IF NOT EXISTS idx_meetups_location  ON meetups(location);
CREATE INDEX IF NOT EXISTS idx_meetups_owner_id  ON meetups(owner_id);