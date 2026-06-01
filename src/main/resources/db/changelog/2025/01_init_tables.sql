-- Создание таблицы roles
CREATE TABLE roles
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255)
);

-- Создание таблицы groups
CREATE TABLE groups
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255)
);

-- Создание таблицы subgroups
CREATE TABLE subgroups
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(255),
    group_id UUID NOT NULL,

    FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

-- Создание таблицы users
CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255),
    username        VARCHAR(255),
    email       VARCHAR(255),
    password    VARCHAR(255),
    is_disabled BOOLEAN,
    role_id     UUID,

    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Создание таблицы tokens
CREATE TABLE tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255),
    created_at TIMESTAMP,
    expires_at TIMESTAMP,
    token_type VARCHAR(255),
    user_id    UUID,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);