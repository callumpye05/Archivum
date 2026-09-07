package com.cal.archivum.repository;

import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CharacterRepositoryTest {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private WorldRepository worldRepository;

    private World testWorld;
    private WorldCharacter testCharacter;

    @BeforeEach
    void setUp() {

        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc("World used for repository testing");

        testWorld = worldRepository.save(testWorld);

        testCharacter = new WorldCharacter();
        testCharacter.setCharacterName("Test Character");
        testCharacter.setCharacterSpecies("Human");
        testCharacter.setAge(25);
        testCharacter.setCharacterDescription("Repository test character");
        testCharacter.setCharacterNationality("Test");
        testCharacter.setWorld(testWorld);

        testCharacter = characterRepository.save(testCharacter);
    }

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

    @Test
    void findAllByWorld_shouldReturnCharactersBelongingToWorld() {

        WorldCharacter secondCharacter = new WorldCharacter();
        secondCharacter.setCharacterName("Second Character");
        secondCharacter.setCharacterSpecies("Human");
        secondCharacter.setAge(30);
        secondCharacter.setWorld(testWorld);

        characterRepository.save(secondCharacter);

        List<WorldCharacter> result =
                characterRepository.findAllByWorld(testWorld);

        assertEquals(2, result.size());
    }

    @Test
    void findByCharacterIdAndWorldWorldId_shouldReturnCharacter_whenBothMatch() {

        Optional<WorldCharacter> result =
                characterRepository.findByCharacterIdAndWorldWorldId(
                        testCharacter.getCharacterId(),
                        testWorld.getWorldId()
                );

        assertTrue(result.isPresent());

        assertEquals(
                testCharacter.getCharacterId(),
                result.get().getCharacterId()
        );
    }

    @Test
    void findByCharacterIdAndWorldWorldId_shouldReturnEmpty_whenWorldDoesNotMatch() {

        World otherWorld = new World();
        otherWorld.setWorldName("Other World");
        otherWorld.setWorldDesc("Different world");

        otherWorld = worldRepository.save(otherWorld);

        Optional<WorldCharacter> result =
                characterRepository.findByCharacterIdAndWorldWorldId(
                        testCharacter.getCharacterId(),
                        otherWorld.getWorldId()
                );

        assertTrue(result.isEmpty());
    }
}
