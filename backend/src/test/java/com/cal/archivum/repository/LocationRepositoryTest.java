package com.cal.archivum.repository;

import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.cal.archivum.enums.LocationType.CITY;
import static com.cal.archivum.enums.LocationType.VILLAGE;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepo;

    @Autowired
    private WorldRepository worldRepo;

    @Autowired
    private UserRepository userRepo;

    private User testUser;
    private User otherUser;

    private World testWorld;
    private World otherWorld;

    private Location testLocation;


    @BeforeEach
    void setUp() {

        // =====================================================
        // USERS
        // =====================================================

        testUser = new User();
        testUser.setUserName("test_user");
        testUser.setEmail("test@archivum.local");
        testUser.setUserHashedPassword("{noop}password");

        testUser = userRepo.save(testUser);


        otherUser = new User();
        otherUser.setUserName("other_user");
        otherUser.setEmail("other@archivum.local");
        otherUser.setUserHashedPassword("{noop}password");

        otherUser = userRepo.save(otherUser);


        // =====================================================
        // WORLDS
        // =====================================================

        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc(
                "World used for repository testing"
        );
        testWorld.setOwner(testUser);

        testWorld = worldRepo.save(testWorld);


        otherWorld = new World();
        otherWorld.setWorldName("Other World");
        otherWorld.setWorldDesc(
                "World belonging to another user"
        );
        otherWorld.setOwner(otherUser);

        otherWorld = worldRepo.save(otherWorld);


        // =====================================================
        // LOCATION
        // =====================================================

        testLocation = new Location();
        testLocation.setLocationName("Test Location");
        testLocation.setLocationType(CITY);
        testLocation.setLocationDescription(
                "Repository test location"
        );
        testLocation.setWorld(testWorld);

        testLocation = locationRepo.save(testLocation);
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void findById_shouldReturnLocation_whenLocationExists() {

        Optional<Location> result =
                locationRepo.findById(
                        testLocation.getId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "Test Location",
                result.get().getLocationName()
        );
    }


    // =========================================================
    // FIND ALL BY WORLD
    // =========================================================

    @Test
    void findAllByWorld_shouldReturnOnlyLocationsBelongingToWorld() {

        Location secondLocation =
                new Location();

        secondLocation.setLocationName(
                "Second Location"
        );
        secondLocation.setLocationType(VILLAGE);
        secondLocation.setLocationDescription(
                "Another test location"
        );
        secondLocation.setWorld(testWorld);

        locationRepo.save(secondLocation);


        Location otherLocation =
                new Location();

        otherLocation.setLocationName(
                "Other User Location"
        );
        otherLocation.setLocationType(CITY);
        otherLocation.setLocationDescription(
                "Should not be returned"
        );
        otherLocation.setWorld(otherWorld);

        locationRepo.save(otherLocation);


        List<Location> result =
                locationRepo.findAllByWorld(
                        testWorld
                );

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(location ->
                                location.getWorld()
                                        .getWorldId()
                                        .equals(
                                                testWorld.getWorldId()
                                        )
                        )
        );
    }


    // =========================================================
    // FIND BY LOCATION ID + OWNER
    // =========================================================

    @Test
    void findByIdAndWorldOwner_shouldReturnLocation_whenOwnerMatches() {

        Optional<Location> result =
                locationRepo
                        .findByIdAndWorldOwner(
                                testLocation.getId(),
                                testUser
                        );

        assertTrue(result.isPresent());

        assertEquals(
                testLocation.getId(),
                result.get().getId()
        );

        assertSame(
                testUser,
                result.get()
                        .getWorld()
                        .getOwner()
        );
    }


    @Test
    void findByIdAndWorldOwner_shouldReturnEmpty_whenOwnerDoesNotMatch() {

        Optional<Location> result =
                locationRepo
                        .findByIdAndWorldOwner(
                                testLocation.getId(),
                                otherUser
                        );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND BY LOCATION ID + WORLD ID + OWNER
    // =========================================================

    @Test
    void findByIdAndWorldWorldIdAndWorldOwner_shouldReturnLocation_whenEverythingMatches() {

        Optional<Location> result =
                locationRepo
                        .findByIdAndWorldWorldIdAndWorldOwner(
                                testLocation.getId(),
                                testWorld.getWorldId(),
                                testUser
                        );

        assertTrue(result.isPresent());

        assertEquals(
                testLocation.getId(),
                result.get().getId()
        );

        assertEquals(
                testWorld.getWorldId(),
                result.get()
                        .getWorld()
                        .getWorldId()
        );

        assertSame(
                testUser,
                result.get()
                        .getWorld()
                        .getOwner()
        );
    }


    @Test
    void findByIdAndWorldWorldIdAndWorldOwner_shouldReturnEmpty_whenWorldDoesNotMatch() {

        Optional<Location> result =
                locationRepo
                        .findByIdAndWorldWorldIdAndWorldOwner(
                                testLocation.getId(),
                                otherWorld.getWorldId(),
                                testUser
                        );

        assertTrue(result.isEmpty());
    }


    @Test
    void findByIdAndWorldWorldIdAndWorldOwner_shouldReturnEmpty_whenOwnerDoesNotMatch() {

        Optional<Location> result =
                locationRepo
                        .findByIdAndWorldWorldIdAndWorldOwner(
                                testLocation.getId(),
                                testWorld.getWorldId(),
                                otherUser
                        );

        assertTrue(result.isEmpty());
    }
}