INSERT INTO users (username, password, email, registered) VALUES
('John', '$2a$10$skRcf.P8fjn.PVDeJQHGruvaL9wL.Ve6FkHqYTxpLSYmEHersaw2C', 'john@smith.com', CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) VALUES
('1', 'DISPATCHER');