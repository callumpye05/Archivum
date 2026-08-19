package com.cal.archivum.repository;

import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.cal.archivum.enums.LocationType.CITY;
import static com.cal.archivum.enums.LocationType.VILLAGE;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private WorldRepository worldRepo;

    private World testWorld;
    private Location testLocation;

    @BeforeEach
    void setUp() {

        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc("World used for repository testing");

        testWorld = worldRepo.save(testWorld);

        testLocation = new Location();
        testLocation.setLocationName("Test Location");
        testLocation.setLocationType(CITY);
        testLocation.setLocationDescription("Repository test location");
        testLocation.setWorld(testWorld);
        testLocation = locationRepo.save(testLocation);
    }

    @Test
    void findById_shouldReturnLocation_whenLocationExists() {

        Optional<Location> result =locationRepo.findById(testLocation.getId());
        assertTrue(result.isPresent());
        assertEquals("Test Location", result.get().getLocationName());
    }

    @Test
    void findAllByWorld_shouldReturnLocationsBelongingToWorld() {

        Location secondLocation = new Location();
        secondLocation.setLocationName("Second Location");
        secondLocation.setLocationType(VILLAGE);
        secondLocation.setLocationDescription("test");
        secondLocation.setWorld(testWorld);

        locationRepo.save(secondLocation);
        List<Location> result = locationRepo.findAllByWorld(testWorld);
        assertEquals(2, result.size());
    }

    @Test
    void findByIdAndWorldWorldId_shouldReturnLocation_whenBothMatch() {

        Optional<Location> result =locationRepo.findByIdAndWorldWorldId(testLocation.getId(), testWorld.getWorldId());
        assertTrue(result.isPresent());
        assertEquals(testLocation.getId(), result.get().getId());
    }

    @Test
    void findByIdAndWorldWorldId_shouldReturnEmpty_whenWorldDoesNotMatch() {

        World otherWorld = new World();
        otherWorld.setWorldName("Other World");
        otherWorld.setWorldDesc("Different world");
        otherWorld = worldRepo.save(otherWorld);
        Optional<Location> result = locationRepo.findByIdAndWorldWorldId(testLocation.getId(), otherWorld.getWorldId());
        assertTrue(result.isEmpty());
    }
}
