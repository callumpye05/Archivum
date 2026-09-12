package com.cal.archivum.repository;

import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;

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
class CharacterRepositoryTest {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private WorldRepository worldRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    private World testWorld;
    private World otherWorld;

    private WorldCharacter testCharacter;


    @BeforeEach
    void setUp() {

        // =====================================================
        // USERS
        // =====================================================

        testUser = new User();
        testUser.setUserName("test_user");
        testUser.setEmail("test@archivum.local");
        testUser.setUserHashedPassword("{noop}password");

        testUser = userRepository.save(testUser);


        otherUser = new User();
        otherUser.setUserName("other_user");
        otherUser.setEmail("other@archivum.local");
        otherUser.setUserHashedPassword("{noop}password");

        otherUser = userRepository.save(otherUser);


        // =====================================================
        // WORLDS
        // =====================================================

        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc(
                "World used for repository testing"
        );
        testWorld.setOwner(testUser);

        testWorld = worldRepository.save(testWorld);


        otherWorld = new World();
        otherWorld.setWorldName("Other World");
        otherWorld.setWorldDesc(
                "World owned by another user"
        );
        otherWorld.setOwner(otherUser);

        otherWorld = worldRepository.save(otherWorld);


        // =====================================================
        // CHARACTER
        // =====================================================

        testCharacter = new WorldCharacter();
        testCharacter.setCharacterName("Test Character");
        testCharacter.setCharacterSpecies("Human");
        testCharacter.setAge(25);
        testCharacter.setCharacterDescription(
                "Repository test character"
        );
        testCharacter.setCharacterNationality("Test");
        testCharacter.setWorld(testWorld);

        testCharacter =
                characterRepository.save(testCharacter);
    }


    // =========================================================
    // STANDARD FIND BY ID
    // =========================================================

    @Test
    void findById_shouldReturnCharacter_whenCharacterExists() {

        Optional<WorldCharacter> result =
                characterRepository.findById(
                        testCharacter.getCharacterId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                "Test Character",
                result.get().getCharacterName()
        );
    }


    // =========================================================
    // FIND ALL BY WORLD
    // =========================================================

    @Test
    void findAllByWorld_shouldReturnOnlyCharactersBelongingToWorld() {

        WorldCharacter secondCharacter =
                new WorldCharacter();

        secondCharacter.setCharacterName(
                "Second Character"
        );
        secondCharacter.setCharacterSpecies("Human");
        secondCharacter.setAge(30);
        secondCharacter.setCharacterDescription(
                "Another character"
        );
        secondCharacter.setCharacterNationality(
                "Test"
        );
        secondCharacter.setWorld(testWorld);

        characterRepository.save(secondCharacter);


        WorldCharacter otherCharacter =
                new WorldCharacter();

        otherCharacter.setCharacterName(
                "Other User Character"
        );
        otherCharacter.setCharacterSpecies("Human");
        otherCharacter.setAge(40);
        otherCharacter.setCharacterDescription(
                "Should not be returned"
        );
        otherCharacter.setCharacterNationality(
                "Other"
        );
        otherCharacter.setWorld(otherWorld);

        characterRepository.save(otherCharacter);


        List<WorldCharacter> result =
                characterRepository.findAllByWorld(
                        testWorld
                );

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(character ->
                                character.getWorld()
                                        .getWorldId()
                                        .equals(
                                                testWorld.getWorldId()
                                        )
                        )
        );
    }


    // =========================================================
    // FIND BY CHARACTER ID + OWNER
    // =========================================================

    @Test
    void findByCharacterIdAndWorldOwner_shouldReturnCharacter_whenOwnerMatches() {

        Optional<WorldCharacter> result =
                characterRepository
                        .findByCharacterIdAndWorldOwner(
                                testCharacter
                                        .getCharacterId(),
                                testUser
                        );

        assertTrue(result.isPresent());

        assertEquals(
                testCharacter.getCharacterId(),
                result.get().getCharacterId()
        );

        assertEquals(
                testUser.getId(),
                result.get()
                        .getWorld()
                        .getOwner()
                        .getId()
        );
    }


    @Test
    void findByCharacterIdAndWorldOwner_shouldReturnEmpty_whenOwnerDoesNotMatch() {

        Optional<WorldCharacter> result =
                characterRepository
                        .findByCharacterIdAndWorldOwner(
                                testCharacter
                                        .getCharacterId(),
                                otherUser
                        );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND BY CHARACTER ID + WORLD ID + OWNER
    // =========================================================

    @Test
    void findByCharacterIdAndWorldWorldIdAndWorldOwner_shouldReturnCharacter_whenEverythingMatches() {

        Optional<WorldCharacter> result =
                characterRepository
                        .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                                testCharacter
                                        .getCharacterId(),
                                testWorld.getWorldId(),
                                testUser
                        );

        assertTrue(result.isPresent());

        assertEquals(
                testCharacter.getCharacterId(),
                result.get().getCharacterId()
        );

        assertEquals(
                testWorld.getWorldId(),
                result.get()
                        .getWorld()
                        .getWorldId()
        );
    }


    @Test
    void findByCharacterIdAndWorldWorldIdAndWorldOwner_shouldReturnEmpty_whenWorldDoesNotMatch() {

        Optional<WorldCharacter> result =
                characterRepository
                        .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                                testCharacter
                                        .getCharacterId(),
                                otherWorld.getWorldId(),
                                testUser
                        );

        assertTrue(result.isEmpty());
    }


    @Test
    void findByCharacterIdAndWorldWorldIdAndWorldOwner_shouldReturnEmpty_whenOwnerDoesNotMatch() {

        Optional<WorldCharacter> result =
                characterRepository
                        .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                                testCharacter
                                        .getCharacterId(),
                                testWorld.getWorldId(),
                                otherUser
                        );

        assertTrue(result.isEmpty());
    }
}