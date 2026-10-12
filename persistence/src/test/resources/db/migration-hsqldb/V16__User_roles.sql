-- HSQLDB version of V16__User_roles.sql
CREATE TABLE IF NOT EXISTS users_roles (
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_name VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role_name)
);

-- Make user with ID 1 a system administrator (role 'ADMIN'), only if previously created
INSERT INTO users_roles (user_id, role_name)
    SELECT u.user_id, 'ADMIN'
    FROM users AS u
    WHERE u.user_id = 1;

-- Retroactively add role 'USER' to existing users
INSERT INTO users_roles (user_id, role_name)
    SELECT u.user_id, 'USER'
    FROM users AS u;
