-- Создание таблицы roles
CREATE TABLE IF NOT EXISTS roles
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255)
);

-- Создание таблицы groups
CREATE TABLE IF NOT EXISTS groups
(
    id   UUID PRIMARY KEY,
    name VARCHAR(255)
);

-- Создание таблицы subgroups
CREATE TABLE IF NOT EXISTS subgroups
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(255),
    group_id UUID NOT NULL,

    FOREIGN KEY (group_id) REFERENCES groups (id) ON DELETE CASCADE
);

-- Создание таблицы users
CREATE TABLE IF NOT EXISTS users
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255),
    username        VARCHAR(255),
    email       VARCHAR(255),
    password    VARCHAR(255),
    token_version    VARCHAR(255),
    is_disabled BOOLEAN,
    role_id     UUID,

    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Создание таблицы tokens
CREATE TABLE IF NOT EXISTS tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255),
    created_at TIMESTAMP,
    expires_at TIMESTAMP,
    position   VARCHAR(255), -- Added this line
    token_type VARCHAR(255),
    user_id    UUID,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Создание таблицы chats
CREATE TABLE IF NOT EXISTS chats
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255),
    email        VARCHAR(255),
    phone        VARCHAR(255),
    is_open      BOOLEAN,
    is_work      BOOLEAN,
    subgroup_id  UUID,
    created_date TIMESTAMP,

    FOREIGN KEY (subgroup_id) REFERENCES subgroups (id)
);

-- Создание таблицы connections
CREATE TABLE IF NOT EXISTS connections
(
    id         UUID PRIMARY KEY,
    chat_id    UUID,
    user_id    UUID,
    start_date TIMESTAMP,
    end_date   TIMESTAMP,

    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Создание таблицы logs
CREATE TABLE IF NOT EXISTS logs
(
    id           UUID PRIMARY KEY,
    text         VARCHAR(255),
    created_date TIMESTAMP,
    user_id      UUID NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Создание таблицы messages
CREATE TABLE IF NOT EXISTS messages
(
    id            UUID PRIMARY KEY,
    text          VARCHAR(255),
    is_support    BOOLEAN,
    created_date  TIMESTAMP,
    connection_id UUID,

    FOREIGN KEY (connection_id) REFERENCES connections (id) ON DELETE CASCADE
);
