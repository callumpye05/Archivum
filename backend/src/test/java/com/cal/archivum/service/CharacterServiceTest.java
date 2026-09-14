package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.User;
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

    @Mock
    private IUserService userService;

    private CharacterService characterService;

    private User testUser;
    private World testWorld;
    private WorldCharacter testCharacter;

    @BeforeEach
    void setUp() {

        characterService =
                new CharacterService(
                        characterRepo,
                        worldRepo,
                        userService
                );

        testUser = new User();
        testUser.setUserName("test_user");

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");
        testWorld.setOwner(testUser);

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
    void getCharacter_shouldReturnCharacter_whenOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo.findByCharacterIdAndWorldOwner(
                10L,
                testUser
        )).thenReturn(Optional.of(testCharacter));

        WorldCharacter result =
                characterService.getCharacter(10L);

        assertNotNull(result);
        assertEquals(10L, result.getCharacterId());
        assertEquals(
                "Lucian Varek",
                result.getCharacterName()
        );

        verify(characterRepo)
                .findByCharacterIdAndWorldOwner(
                        10L,
                        testUser
                );
    }

    @Test
    void getCharacter_shouldThrow_whenCharacterNotAccessibleToCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo.findByCharacterIdAndWorldOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService.getCharacter(99L)
        );

        verify(characterRepo)
                .findByCharacterIdAndWorldOwner(
                        99L,
                        testUser
                );
    }


    // =========================================================
    // GET ALL CHARACTERS FROM WORLD
    // =========================================================

    @Test
    void getAllCharactersFromWorld_shouldReturnCharacters_whenUserOwnsWorld() {

        WorldCharacter secondCharacter =
                new WorldCharacter();

        secondCharacter.setCharacterId(11L);
        secondCharacter.setCharacterName("Mira Kohl");
        secondCharacter.setWorld(testWorld);

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepo.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(characterRepo.findAllByWorld(testWorld))
                .thenReturn(List.of(
                        testCharacter,
                        secondCharacter
                ));

        List<WorldCharacter> result =
                characterService
                        .getAllCharactersFromWorld(2L);

        assertEquals(2, result.size());

        assertEquals(
                "Lucian Varek",
                result.get(0).getCharacterName()
        );

        assertEquals(
                "Mira Kohl",
                result.get(1).getCharacterName()
        );

        verify(worldRepo)
                .findByWorldIdAndOwner(
                        2L,
                        testUser
                );

        verify(characterRepo)
                .findAllByWorld(testWorld);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturnEmptyList_whenOwnedWorldHasNoCharacters() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepo.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(characterRepo.findAllByWorld(testWorld))
                .thenReturn(List.of());

        List<WorldCharacter> result =
                characterService
                        .getAllCharactersFromWorld(2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(characterRepo)
                .findAllByWorld(testWorld);
    }

    @Test
    void getAllCharactersFromWorld_shouldThrow_whenWorldNotOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepo.findByWorldIdAndOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> characterService
                        .getAllCharactersFromWorld(99L)
        );

        verify(characterRepo, never())
                .findAllByWorld(any());
    }


    // =========================================================
    // GET CHARACTER BY WORLD
    // =========================================================

    @Test
    void getCharacterByWorldId_shouldReturnCharacter_whenWorldAndOwnerMatch() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        2L,
                        testUser
                ))
                .thenReturn(Optional.of(testCharacter));

        WorldCharacter result =
                characterService
                        .getCharacterByWorldId(2L, 10L);

        assertNotNull(result);
        assertEquals(
                10L,
                result.getCharacterId()
        );

        assertEquals(
                2L,
                result.getWorld().getWorldId()
        );

        verify(characterRepo)
                .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        2L,
                        testUser
                );
    }

    @Test
    void getCharacterByWorldId_shouldThrow_whenCharacterNotAccessibleThroughWorld() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        3L,
                        testUser
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFoundByWorld.class,
                () -> characterService
                        .getCharacterByWorldId(3L, 10L)
        );
    }


    // =========================================================
    // CREATE CHARACTER
    // =========================================================

    @Test
    void createCharacter_shouldSaveCharacter_whenUserOwnsWorld() {

        CreateCharacterDto dto =
                new CreateCharacterDto(
                        "Lucian Varek",
                        "Human",
                        34,
                        "Test character",
                        "Eisenmarkian"
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepo.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(characterRepo.save(
                any(WorldCharacter.class)
        ))
                .thenAnswer(invocation -> {
                    WorldCharacter character =
                            invocation.getArgument(0);

                    character.setCharacterId(10L);
                    return character;
                });

        WorldCharacter result =
                characterService
                        .createCharacter(dto, 2L);

        assertNotNull(result);

        assertEquals(
                "Lucian Varek",
                result.getCharacterName()
        );

        assertEquals(
                "Human",
                result.getCharacterSpecies()
        );

        assertEquals(
                34,
                result.getAge()
        );

        assertEquals(
                "Eisenmarkian",
                result.getCharacterNationality()
        );

        assertEquals(
                "Test character",
                result.getCharacterDescription()
        );

        assertSame(
                testWorld,
                result.getWorld()
        );

        verify(worldRepo)
                .findByWorldIdAndOwner(
                        2L,
                        testUser
                );

        verify(characterRepo)
                .save(any(WorldCharacter.class));
    }

    @Test
    void createCharacter_shouldThrow_whenUserDoesNotOwnWorld() {

        CreateCharacterDto dto =
                new CreateCharacterDto(
                        "Lucian Varek",
                        "Human",
                        34,
                        "Test character",
                        "Eisenmarkian"
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepo.findByWorldIdAndOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> characterService
                        .createCharacter(dto, 99L)
        );

        verify(characterRepo, never())
                .save(any());
    }


    // =========================================================
    // UPDATE CHARACTER
    // =========================================================

    @Test
    void updateCharacter_shouldUpdateProvidedFields_whenOwnedByCurrentUser() {

        UpdateCharacterDto dto =
                new UpdateCharacterDto(
                        "Lucian Updated",
                        null,
                        35,
                        "Updated description",
                        null
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldOwner(
                        10L,
                        testUser
                ))
                .thenReturn(Optional.of(testCharacter));

        when(characterRepo.save(testCharacter))
                .thenReturn(testCharacter);

        WorldCharacter result =
                characterService
                        .updateCharacter(10L, dto);

        assertEquals(
                "Lucian Updated",
                result.getCharacterName()
        );

        assertEquals(
                35,
                result.getAge()
        );

        assertEquals(
                "Updated description",
                result.getCharacterDescription()
        );

        // Null fields remain untouched
        assertEquals(
                "Human",
                result.getCharacterSpecies()
        );

        assertEquals(
                "Eisenmarkian",
                result.getCharacterNationality()
        );

        verify(characterRepo)
                .save(testCharacter);
    }

    @Test
    void updateCharacter_shouldOnlyUpdateNonNullFields() {

        UpdateCharacterDto dto =
                new UpdateCharacterDto(
                        null,
                        "Half-Devil",
                        null,
                        null,
                        "Alta-Rivan"
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldOwner(
                        10L,
                        testUser
                ))
                .thenReturn(Optional.of(testCharacter));

        when(characterRepo.save(testCharacter))
                .thenReturn(testCharacter);

        WorldCharacter result =
                characterService
                        .updateCharacter(10L, dto);

        assertEquals(
                "Lucian Varek",
                result.getCharacterName()
        );

        assertEquals(
                "Half-Devil",
                result.getCharacterSpecies()
        );

        assertEquals(
                34,
                result.getAge()
        );

        assertEquals(
                "Test character",
                result.getCharacterDescription()
        );

        assertEquals(
                "Alta-Rivan",
                result.getCharacterNationality()
        );
    }

    @Test
    void updateCharacter_shouldThrow_whenCharacterNotOwnedByCurrentUser() {

        UpdateCharacterDto dto =
                new UpdateCharacterDto(
                        "Updated",
                        null,
                        null,
                        null,
                        null
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldOwner(
                        99L,
                        testUser
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService
                        .updateCharacter(99L, dto)
        );

        verify(characterRepo, never())
                .save(any());
    }


    // =========================================================
    // DELETE CHARACTER
    // =========================================================

    @Test
    void deleteCharacter_shouldDelete_whenCharacterOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldOwner(
                        10L,
                        testUser
                ))
                .thenReturn(Optional.of(testCharacter));

        characterService.deleteCharacter(10L);

        verify(characterRepo)
                .findByCharacterIdAndWorldOwner(
                        10L,
                        testUser
                );

        verify(characterRepo)
                .delete(testCharacter);
    }

    @Test
    void deleteCharacter_shouldThrow_whenCharacterNotOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(characterRepo
                .findByCharacterIdAndWorldOwner(
                        99L,
                        testUser
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                CharacterNotFound.class,
                () -> characterService
                        .deleteCharacter(99L)
        );

        verify(characterRepo, never())
                .delete(any(WorldCharacter.class));
    }


    // =========================================================
    // TRANSFORM DTO
    // =========================================================

    @Test
    void transformFromDto_shouldCreateCharacterWithCorrectWorld() {

        CreateCharacterDto dto =
                new CreateCharacterDto(
                        "Lucian Varek",
                        "Human",
                        34,
                        "Test character",
                        "Eisenmarkian"
                );

        WorldCharacter result =
                characterService
                        .transformFromDto(dto, testWorld);

        assertEquals(
                "Lucian Varek",
                result.getCharacterName()
        );

        assertEquals(
                "Human",
                result.getCharacterSpecies()
        );

        assertEquals(
                34,
                result.getAge()
        );

        assertEquals(
                "Test character",
                result.getCharacterDescription()
        );

        assertEquals(
                "Eisenmarkian",
                result.getCharacterNationality()
        );

        assertSame(
                testWorld,
                result.getWorld()
        );
    }
}