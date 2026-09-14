package com.cal.archivum.repository;

import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WorldRepositoryTest {

    @Autowired
    private WorldRepository worldRepo;

    @Autowired
    private UserRepository userRepo;

    private User testUser;
    private User otherUser;

    private World testWorld;
    private World testWorld2;
    private World otherUserWorld;


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
        // TEST USER WORLDS
        // =====================================================

        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc(
                "World used for repository testing"
        );
        testWorld.setOwner(testUser);

        testWorld = worldRepo.save(testWorld);


        testWorld2 = new World();
        testWorld2.setWorldName("Test World 2");
        testWorld2.setWorldDesc(
                "Second world used for repository testing"
        );
        testWorld2.setOwner(testUser);

        testWorld2 = worldRepo.save(testWorld2);


        // =====================================================
        // OTHER USER WORLD
        // =====================================================

        otherUserWorld = new World();
        otherUserWorld.setWorldName("Other User World");
        otherUserWorld.setWorldDesc(
                "World belonging to another user"
        );
        otherUserWorld.setOwner(otherUser);

        otherUserWorld = worldRepo.save(otherUserWorld);
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void findById_shouldReturnWorld_whenWorldExists() {

        Optional<World> result =
                worldRepo.findById(
                        testWorld.getWorldId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "Test World",
                result.get().getWorldName()
        );
    }


    // =========================================================
    // FIND BY WORLD ID + OWNER
    // =========================================================

    @Test
    void findByWorldIdAndOwner_shouldReturnWorld_whenOwnerMatches() {

        Optional<World> result =
                worldRepo.findByWorldIdAndOwner(
                        testWorld.getWorldId(),
                        testUser
                );

        assertTrue(result.isPresent());

        assertEquals(
                testWorld.getWorldId(),
                result.get().getWorldId()
        );

        assertEquals(
                "test_user",
                result.get().getOwner().getUserName()
        );
    }


    @Test
    void findByWorldIdAndOwner_shouldReturnEmpty_whenOwnerDoesNotMatch() {

        Optional<World> result =
                worldRepo.findByWorldIdAndOwner(
                        testWorld.getWorldId(),
                        otherUser
                );

        assertTrue(result.isEmpty());
    }


    @Test
    void findByWorldIdAndOwner_shouldReturnEmpty_whenWorldDoesNotExist() {

        Optional<World> result =
                worldRepo.findByWorldIdAndOwner(
                        999L,
                        testUser
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND ALL BY OWNER
    // =========================================================

    @Test
    void findAllByOwner_shouldReturnOnlyWorldsOwnedByUser() {

        List<World> result =
                worldRepo.findAllByOwner(testUser);

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(world ->
                                world.getOwner()
                                        .getUserName()
                                        .equals("test_user")
                        )
        );
    }


    @Test
    void findAllByOwner_shouldNotReturnWorldsOwnedByOtherUsers() {

        List<World> result =
                worldRepo.findAllByOwner(testUser);

        assertFalse(
                result.stream()
                        .anyMatch(world ->
                                world.getWorldId()
                                        .equals(
                                                otherUserWorld.getWorldId()
                                        )
                        )
        );
    }


    @Test
    void findAllByOwner_shouldReturnEmptyList_whenUserOwnsNoWorlds() {

        User userWithNoWorlds = new User();
        userWithNoWorlds.setUserName("empty_user");
        userWithNoWorlds.setEmail(
                "empty@archivum.local"
        );
        userWithNoWorlds.setUserHashedPassword(
                "{noop}password"
        );

        userWithNoWorlds =
                userRepo.save(userWithNoWorlds);

        List<World> result =
                worldRepo.findAllByOwner(
                        userWithNoWorlds
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}