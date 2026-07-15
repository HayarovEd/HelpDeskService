-- Add a new group for unknown users
INSERT INTO "groups" (id, name, oper_id)
VALUES ('00000000-0000-0000-0000-000000000009', 'Незарегистированные', 111111);

INSERT INTO
    roles (id, name)
VALUES
    ('00000000-0000-0000-0000-000000000004', 'UNREGISTERED_USER');