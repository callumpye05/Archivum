package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.exception.CharacterNotFound;
import com.cal.archivum.exception.CharacterNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.impl.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepo;

    @Mock
    private WorldRepository worldRepo;

    private CharacterService characterService;

    private World testWorld;
    private WorldCharacter testCharacter;

    @BeforeEach
    void setUp() {

        characterService = new CharacterService(characterRepo, worldRepo);

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");

        testCharacter = new WorldCharacter();
        testCharacter.setCharacterId(10L);
        testCharacter.setCharacterName("Lucian Varek");
        testCharacter.setCharacterSpecies("Human");
        testCharacter.setAge(34);
        testCharacter.setCharacterNationality("Eisenmarkian");
        testCharacter.setCharacterDescription("Test character");
        testCharacter.setWorld(testWorld);
    }

    // =========================================================
    // GET CHARACTER
    // =========================================================

    @Test
    void getCharacter_shouldReturnCharacter_whenCharacterExists() {

        when(characterRepo.findById(10L))
                .thenReturn(Optional.of(testCharacter));

        WorldCharacter result = characterService.getCharacter(10L);

        assertNotNull(result);
        assertEquals(10L, result.getCharacterId());
        assertEquals("Lucian Varek", result.getCharacterName());

        verify(characterRepo).findById(10L);
    }

    @Test
    void getCharacter_shouldThrowCharacterNotFound_whenCharacterDoesNotExist() {

        when(characterRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService.getCharacter(99L)
        );

        verify(characterRepo).findById(99L);
    }


    // =========================================================
    // GET ALL CHARACTERS FROM WORLD
    // =========================================================

    @Test
    void getAllCharactersFromWorld_shouldReturnCharacters_whenWorldExists() {

        WorldCharacter secondCharacter = new WorldCharacter();
        secondCharacter.setCharacterId(11L);
        secondCharacter.setCharacterName("Mira Kohl");
        secondCharacter.setWorld(testWorld);

        when(worldRepo.findById(2L))
                .thenReturn(Optional.of(testWorld));

        when(characterRepo.findAllByWorld(testWorld))
                .thenReturn(List.of(testCharacter, secondCharacter));

        List<WorldCharacter> result =
                characterService.getAllCharactersFromWorld(2L);

        assertEquals(2, result.size());
        assertEquals("Lucian Varek", result.get(0).getCharacterName());
        assertEquals("Mira Kohl", result.get(1).getCharacterName());

        verify(worldRepo).findById(2L);
        verify(characterRepo).findAllByWorld(testWorld);
    }

    @Test
    void getAllCharactersFromWorld_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        when(worldRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> characterService.getAllCharactersFromWorld(99L)
        );

        verify(worldRepo).findById(99L);

        // Important: repository should never be queried for characters
        verify(characterRepo, never()).findAllByWorld(any());
    }


    // =========================================================
    // GET CHARACTER BY WORLD
    // =========================================================

    @Test
    void getCharacterByWorldId_shouldReturnCharacter_whenCharacterBelongsToWorld() {

        when(characterRepo.findByCharacterIdAndWorldWorldId(10L, 2L))
                .thenReturn(Optional.of(testCharacter));

        WorldCharacter result =
                characterService.getCharacterByWorldId(2L, 10L);

        assertNotNull(result);
        assertEquals(10L, result.getCharacterId());
        assertEquals(2L, result.getWorld().getWorldId());

        verify(characterRepo)
                .findByCharacterIdAndWorldWorldId(10L, 2L);
    }

    @Test
    void getCharacterByWorldId_shouldThrow_whenCharacterDoesNotBelongToWorld() {

        when(characterRepo.findByCharacterIdAndWorldWorldId(10L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFoundByWorld.class,
                () -> characterService.getCharacterByWorldId(3L, 10L)
        );

        verify(characterRepo)
                .findByCharacterIdAndWorldWorldId(10L, 3L);
    }


    // =========================================================
    // CREATE CHARACTER
    // =========================================================

    @Test
    void createCharacter_shouldSaveCharacter_whenWorldExists() {

        /*
         * Adjust this constructor if your CreateCharacterDto
         * fields are ordered differently.
         */
        CreateCharacterDto dto = new CreateCharacterDto(
                "Lucian Varek",
                "Human",
                34,
                "Test character",
                "Eisenmarkian"
        );

        when(worldRepo.findById(2L))
                .thenReturn(Optional.of(testWorld));

        when(characterRepo.save(any(WorldCharacter.class)))
                .thenAnswer(invocation -> {
                    WorldCharacter character = invocation.getArgument(0);
                    character.setCharacterId(10L);
                    return character;
                });

        WorldCharacter result =
                characterService.createCharacter(dto, 2L);

        assertNotNull(result);
        assertEquals("Lucian Varek", result.getCharacterName());
        assertEquals("Human", result.getCharacterSpecies());
        assertEquals(34, result.getAge());
        assertEquals(testWorld, result.getWorld());

        verify(characterRepo).save(any(WorldCharacter.class));
    }

    @Test
    void createCharacter_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        CreateCharacterDto dto = new CreateCharacterDto(
                "Lucian Varek",
                "Human",
                34,
                "Test character",
                "Eisenmarkian"
        );

        when(worldRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> characterService.createCharacter(dto, 99L)
        );

        verify(characterRepo, never()).save(any());
    }


    // =========================================================
    // UPDATE CHARACTER
    // =========================================================

    @Test
    void updateCharacter_shouldUpdateProvidedFields() {

        UpdateCharacterDto dto = new UpdateCharacterDto(
                "Lucian Updated",
                null,
                35,
                "Updated description",
                null
        );

        when(characterRepo.findById(10L))
                .thenReturn(Optional.of(testCharacter));

        when(characterRepo.save(testCharacter))
                .thenReturn(testCharacter);

        WorldCharacter result =
                characterService.updateCharacter(10L, dto);

        assertEquals("Lucian Updated", result.getCharacterName());
        assertEquals(35, result.getAge());
        assertEquals("Updated description", result.getCharacterDescription());

        // Existing fields should remain untouched when DTO values are null
        assertEquals("Human", result.getCharacterSpecies());
        assertEquals("Eisenmarkian", result.getCharacterNationality());

        verify(characterRepo).save(testCharacter);
    }

    @Test
    void updateCharacter_shouldThrowCharacterNotFound_whenCharacterDoesNotExist() {

        UpdateCharacterDto dto = new UpdateCharacterDto(
                "Updated",
                null,
                null,
                null,
                null
        );

        when(characterRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService.updateCharacter(99L, dto)
        );

        verify(characterRepo, never()).save(any());
    }


    // =========================================================
    // DELETE CHARACTER
    // =========================================================

    @Test
    void deleteCharacter_shouldDeleteCharacter_whenCharacterExists() {

        when(characterRepo.findById(10L))
                .thenReturn(Optional.of(testCharacter));

        characterService.deleteCharacter(10L);

        verify(characterRepo).findById(10L);
        verify(characterRepo).deleteById(10L);
    }

    @Test
    void deleteCharacter_shouldThrowCharacterNotFound_whenCharacterDoesNotExist() {

        when(characterRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService.deleteCharacter(99L)
        );

        verify(characterRepo, never()).deleteById(anyLong());
    }
}