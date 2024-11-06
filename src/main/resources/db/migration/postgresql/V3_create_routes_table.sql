CREATE TABLE routes (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_location VARCHAR(100) NOT NULL,
    end_location VARCHAR(100) NOT NULL,
    distance DECIMAL(6, 2) NOT NULL,
    estimated_time INT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'planned',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    vehicle_id INT REFERENCES vehicles(id),
    user_id INT REFERENCES users(id)
);