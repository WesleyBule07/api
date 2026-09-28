-- name recommendations tables
CREATE TABLE name_record (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(80) NOT NULL UNIQUE,
    gender     VARCHAR(30) NOT NULL,
    origin     VARCHAR(60),
    province   VARCHAR(60),
    meaning    VARCHAR(255),
    popularity INT         NOT NULL
);

CREATE TABLE user_favorite (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES app_user (id)            ON DELETE CASCADE,
    name_id    BIGINT       NOT NULL REFERENCES name_record (id)          ON DELETE CASCADE,
    created_at TIMESTAMPTZ  NOT NULL,
    UNIQUE (user_id, name_id)
);

CREATE INDEX idx_user_favorite_user_id ON user_favorite (user_id);
CREATE INDEX idx_name_record_gender ON name_record (gender);
CREATE INDEX idx_name_record_origin ON name_record (origin);