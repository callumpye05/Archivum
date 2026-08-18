
--sample test data generated, using ignore because I already put it in SQLECTRON
INSERT IGNORE INTO worlds (world_name, world_desc, created_at)
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


-- =========================================================
-- SAMPLE CHARACTER DATA
-- =========================================================

INSERT INTO characters
(
    character_name,
    character_species,
    character_age,
    character_desc,
    character_nationality,
    created_at,
    world_id
)
VALUES
(
    'Lucian Varek',
    'Human',
    34,
    'A former military officer who now works as a private security contractor in the industrial districts.',
    'Eisenmarkian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Eisenmark')
),
(
    'Mira Kohl',
    'Human',
    27,
    'An engineer responsible for maintaining automated manufacturing systems beneath the capital.',
    'Eisenmarkian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Eisenmark')
),
(
    'Cassian Moretti',
    'Human',
    42,
    'A well-connected politician belonging to one of Valeria''s oldest political families.',
    'Valerian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Valeria')
),
(
    'Elena Voss',
    'Human',
    24,
    'A young investigative journalist known for exposing corruption among Valerian institutions.',
    'Valerian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Valeria')
),
(
    'Kael',
    'Half-devil',
    31,
    'A hardened wanderer who survives by scavenging abandoned settlements throughout the Ashlands.',
    NULL,
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Ashlands')
),
(
    'Serah Veyn',
    'Half-devil',
    19,
    'A solitary survivor who has adapted to the hostile conditions left behind by the apocalypse.',
    NULL,
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Ashlands')
),
(
    'Dr. Adrian Sol',
    'Human',
    46,
    'A senior researcher specialising in artificial intelligence and autonomous systems.',
    'Novarian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Novaris')
),
(
    'Lyra Chen',
    'Human',
    29,
    'A robotics engineer working on experimental machines intended for hazardous environments.',
    'Novarian',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Novaris')
),


(
    'SPRING_TEST_CHARACTER',
    'Human',
    99,
    'idk',
    'idk',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Eisenmark')
);


-- =========================================================
-- SAMPLE LOCATION DATA
-- =========================================================

INSERT INTO locations
(
    location_name,
    location_type,
    location_desc,
    created_at,
    world_id
)
VALUES
(
    'Ironspire',
    'CITY',
    'A vast industrial metropolis filled with factories, military installations, and dense worker districts.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Eisenmark')
),
(
    'Kronen Works',
    'FACILITY',
    'One of Eisenmark''s largest manufacturing complexes, producing machinery and military equipment.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Eisenmark')
),
(
    'Aurelia',
    'CITY',
    'The political and cultural capital of Valeria, dominated by ancient institutions and wealthy families.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Valeria')
),
(
    'Senatorial Quarter',
    'VILLAGE',
    'An affluent district containing government chambers, embassies, and the estates of powerful families.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Valeria')
),
(
    'The Glass Wastes',
    'VILLAGE',
    'A devastated region where extreme heat transformed sections of the ground into fields of fractured glass.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Ashlands')
),
(
    'Hollow Reach',
    'LANDMARK',
    'The remains of a once-populated city abandoned following the Half-devil apocalypse.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Ashlands')
),
(
    'Helix City',
    'CITY',
    'A technologically advanced city built around universities, laboratories, and research corporations.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Novaris')
),
(
    'Orion Research Institute',
    'FACILITY',
    'A major scientific institution conducting research in robotics, computing, and advanced engineering.',
    CURRENT_TIMESTAMP,
    (SELECT world_id FROM worlds WHERE world_name = 'Novaris')
);