CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    user_name VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );




CREATE TABLE IF NOT EXISTS worlds (

        world_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        world_name VARCHAR(100) NOT NULL,
        world_desc TEXT,
        created_at TIMESTAMP,
        user_id BIGINT NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(user_id),
        UNIQUE (user_id, world_name)
);

CREATE TABLE IF NOT EXISTS characters(

    character_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_name VARCHAR(100) NOT NULL,
    character_species VARCHAR(100) NOT NULL,
    character_age INT NOT NULL,
    character_desc TEXT,
    character_nationality VARCHAR(100), --for now
    created_at TIMESTAMP,
    world_id BIGINT NOT NULL,
    FOREIGN KEY (world_id) REFERENCES worlds(world_id)
);


CREATE TABLE IF NOT EXISTS locations(

    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL,
    location_type VARCHAR(30),
    location_desc TEXT,
    created_at TIMESTAMP,
    world_id BIGINT NOT NULL,
    FOREIGN KEY (world_id) REFERENCES worlds(world_id)
);