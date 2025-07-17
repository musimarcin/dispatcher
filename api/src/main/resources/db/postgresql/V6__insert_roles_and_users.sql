INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_DISPATCHER');
INSERT INTO roles (name) VALUES ('ROLE_DRIVER');

INSERT INTO users (username, password, email, registered) VALUES
('John', '$2a$10$skRcf.P8fjn.PVDeJQHGruvaL9wL.Ve6FkHqYTxpLSYmEHersaw2C', 'john@smith.com', CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES
('1', '2');