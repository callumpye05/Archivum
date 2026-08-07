
CREATE TABLE IF NOT EXISTS Worlds (

        world_id BIGINT AUTO_INCREMENT PRIMARY KEY,
        world_name VARCHAR(100) UNIQUE NOT NULL,
        world_desc TEXT,
        created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Characters(

    character_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_name VARCHAR(100) NOT NULL,
    character_species VARCHAR(100) NOT NULL,
    character_age INT NOT NULL,
    character_desc TEXT,
    character_nationality VARCHAR(100), --for now
    created_at TIMESTAMP,
    world_id BIGINT NOT NULL,
    FOREIGN KEY (world_id) REFERENCES Worlds(world_id)
);


CREATE TABLE IF NOT EXISTS Locations(

    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL,
    location_type VARCHAR(30),
    location_desc TEXT,
    created_at TIMESTAMP,
    world_id BIGINT NOT NULL,
    FOREIGN KEY (world_id) REFERENCES Worlds(world_id)
);