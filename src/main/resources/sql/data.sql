
--sample test data generated, using ignore because I already put it in SQLECTRON
INSERT IGNORE INTO Worlds (world_name, world_desc, created_at)
VALUES
(
    'Highberry',
    'A vast fortified bastion that survived the collapse of the old world.',
    CURRENT_TIMESTAMP
),
(
    'Eisenmark',
    'A heavily industrialized world dominated by dense cities and military infrastructure.',
    CURRENT_TIMESTAMP
),
(
    'Valeria',
    'A prosperous world known for political intrigue, old institutions, and powerful families.',
    CURRENT_TIMESTAMP
),
(
    'Ashlands',
    'A devastated world scarred by the Half-devil apocalypse and largely abandoned.',
    CURRENT_TIMESTAMP
),
(
    'Novaris',
    'A technologically advanced world focused on research, engineering, and scientific development.',
    CURRENT_TIMESTAMP
);