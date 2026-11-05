package com.cal.archivum.service;
import com.cal.archivum.dto.impl.CharacterResponseDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock private CharacterRepository characterRepo;
    @Mock private WorldRepository worldRepo;
    @Mock private IUserService userService;

    private CharacterService characterService;
    private User testUser;
    private World testWorld;
    private WorldCharacter testCharacter;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(characterRepo, worldRepo, userService);
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

    // GET CHARACTER

    @Test
    void getCharacter_shouldReturnCharacter_whenOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(10L, testUser)).thenReturn(Optional.of(testCharacter));

        CharacterResponseDto result = characterService.getCharacter(10L);
        assertNotNull(result);
        assertEquals(10L, result.characterId());
        assertEquals("Lucian Varek", result.characterName());
        assertEquals("Human", result.characterSpecies());
        assertEquals(34, result.age());
        assertEquals("Test character", result.characterDescription());
        assertEquals("Eisenmarkian", result.characterNationality());
        assertEquals(2L, result.worldId());
        verify(characterRepo).findByCharacterIdAndWorldOwner(10L, testUser);
    }

    @Test
    void getCharacter_shouldThrow_whenCharacterNotAccessibleToCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(99L, testUser)).thenReturn(Optional.empty());
        assertThrows(CharacterNotFound.class, () -> characterService.getCharacter(99L));
        verify(characterRepo).findByCharacterIdAndWorldOwner(99L, testUser);
    }

    // GET ALL CHARACTERS FROM WORLD

    @Test
    void getAllCharactersFromWorld_shouldReturnCharacters_whenUserOwnsWorld() {
        WorldCharacter secondCharacter = new WorldCharacter();
        secondCharacter.setCharacterId(11L);
        secondCharacter.setCharacterName("Mira Kohl");
        secondCharacter.setWorld(testWorld);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(2L, testUser)).thenReturn(Optional.of(testWorld));
        when(characterRepo.findAllByWorld(testWorld)).thenReturn(List.of(testCharacter, secondCharacter));

        List<CharacterResponseDto> result = characterService.getAllCharactersFromWorld(2L);
        assertEquals(2, result.size());
        assertEquals("Lucian Varek", result.get(0).characterName());
        assertEquals("Mira Kohl", result.get(1).characterName());
        assertEquals(2L, result.get(0).worldId());
        assertEquals(2L, result.get(1).worldId());
        verify(worldRepo).findByWorldIdAndOwner(2L, testUser);
        verify(characterRepo).findAllByWorld(testWorld);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturnEmptyList_whenOwnedWorldHasNoCharacters() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(2L, testUser)).thenReturn(Optional.of(testWorld));
        when(characterRepo.findAllByWorld(testWorld)).thenReturn(List.of());

        List<CharacterResponseDto> result = characterService.getAllCharactersFromWorld(2L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(characterRepo).findAllByWorld(testWorld);
    }

    @Test
    void getAllCharactersFromWorld_shouldThrow_whenWorldNotOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(99L, testUser)).thenReturn(Optional.empty());
        assertThrows(WorldNotFound.class, () -> characterService.getAllCharactersFromWorld(99L));
        verify(characterRepo, never()).findAllByWorld(any());
    }

    // GET CHARACTER BY WORLD

    @Test
    void getCharacterByWorldId_shouldReturnCharacter_whenWorldAndOwnerMatch() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldWorldIdAndWorldOwner(10L, 2L, testUser)).thenReturn(Optional.of(testCharacter));

        CharacterResponseDto result = characterService.getCharacterByWorldId(2L, 10L);
        assertNotNull(result);
        assertEquals(10L, result.characterId());
        assertEquals(2L, result.worldId());
        verify(characterRepo).findByCharacterIdAndWorldWorldIdAndWorldOwner(10L, 2L, testUser);
    }

    @Test
    void getCharacterByWorldId_shouldThrow_whenCharacterNotAccessibleThroughWorld() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldWorldIdAndWorldOwner(10L, 3L, testUser)).thenReturn(Optional.empty());
        assertThrows(CharacterNotFoundByWorld.class, () -> characterService.getCharacterByWorldId(3L, 10L));
    }

    // CREATE CHARACTER

    @Test
    void createCharacter_shouldSaveCharacter_whenUserOwnsWorld() {
        CreateCharacterDto dto = new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(2L, testUser)).thenReturn(Optional.of(testWorld));
        when(characterRepo.save(any(WorldCharacter.class))).thenAnswer(invocation -> {
            WorldCharacter character = invocation.getArgument(0);
            character.setCharacterId(10L);
            return character;
        });

        CharacterResponseDto result = characterService.createCharacter(dto, 2L);
        assertNotNull(result);
        assertEquals(10L, result.characterId());
        assertEquals("Lucian Varek", result.characterName());
        assertEquals("Human", result.characterSpecies());
        assertEquals(34, result.age());
        assertEquals("Eisenmarkian", result.characterNationality());
        assertEquals("Test character", result.characterDescription());
        assertEquals(2L, result.worldId());

        ArgumentCaptor<WorldCharacter> captor = ArgumentCaptor.forClass(WorldCharacter.class);
        verify(characterRepo).save(captor.capture());
        assertSame(testWorld, captor.getValue().getWorld());
        verify(worldRepo).findByWorldIdAndOwner(2L, testUser);
    }

    @Test
    void createCharacter_shouldUseEntityReturnedBySave() {
        CreateCharacterDto dto = new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(2L, testUser)).thenReturn(Optional.of(testWorld));
        when(characterRepo.save(any(WorldCharacter.class))).thenReturn(testCharacter);

        CharacterResponseDto result = characterService.createCharacter(dto, 2L);
        assertEquals(10L, result.characterId());
        assertEquals("Lucian Varek", result.characterName());
        assertEquals(2L, result.worldId());
    }

    @Test
    void createCharacter_shouldThrow_whenUserDoesNotOwnWorld() {
        CreateCharacterDto dto = new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(99L, testUser)).thenReturn(Optional.empty());
        assertThrows(WorldNotFound.class, () -> characterService.createCharacter(dto, 99L));
        verify(characterRepo, never()).save(any());
    }

    // UPDATE CHARACTER

    @Test
    void updateCharacter_shouldUpdateProvidedFields_whenOwnedByCurrentUser() {
        UpdateCharacterDto dto = new UpdateCharacterDto("Lucian Updated", null, 35, "Updated description", null);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(10L, testUser)).thenReturn(Optional.of(testCharacter));
        when(characterRepo.save(testCharacter)).thenReturn(testCharacter);

        CharacterResponseDto result = characterService.updateCharacter(10L, dto);
        assertEquals("Lucian Updated", result.characterName());
        assertEquals(35, result.age());
        assertEquals("Updated description", result.characterDescription());
        assertEquals("Human", result.characterSpecies());
        assertEquals("Eisenmarkian", result.characterNationality());
        assertEquals(2L, result.worldId());
        verify(characterRepo).save(testCharacter);
    }

    @Test
    void updateCharacter_shouldOnlyUpdateNonNullFields() {
        UpdateCharacterDto dto = new UpdateCharacterDto(null, "Half-Devil", null, null, "Alta-Rivan");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(10L, testUser)).thenReturn(Optional.of(testCharacter));
        when(characterRepo.save(testCharacter)).thenReturn(testCharacter);

        CharacterResponseDto result = characterService.updateCharacter(10L, dto);
        assertEquals("Lucian Varek", result.characterName());
        assertEquals("Half-Devil", result.characterSpecies());
        assertEquals(34, result.age());
        assertEquals("Test character", result.characterDescription());
        assertEquals("Alta-Rivan", result.characterNationality());
        assertEquals(2L, result.worldId());
    }

    @Test
    void updateCharacter_shouldThrow_whenCharacterNotOwnedByCurrentUser() {
        UpdateCharacterDto dto = new UpdateCharacterDto("Updated", null, null, null, null);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(99L, testUser)).thenReturn(Optional.empty());
        assertThrows(CharacterNotFound.class, () -> characterService.updateCharacter(99L, dto));
        verify(characterRepo, never()).save(any());
    }

    // DELETE CHARACTER

    @Test
    void deleteCharacter_shouldDelete_whenCharacterOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(10L, testUser)).thenReturn(Optional.of(testCharacter));
        characterService.deleteCharacter(10L);
        verify(characterRepo).findByCharacterIdAndWorldOwner(10L, testUser);
        verify(characterRepo).delete(testCharacter);
    }

    @Test
    void deleteCharacter_shouldThrow_whenCharacterNotOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(characterRepo.findByCharacterIdAndWorldOwner(99L, testUser)).thenReturn(Optional.empty());
        assertThrows(CharacterNotFound.class, () -> characterService.deleteCharacter(99L));
        verify(characterRepo, never()).delete(any(WorldCharacter.class));
    }

    // MAPPERS

    @Test
    void transformFromDto_shouldCreateCharacterWithCorrectWorld() {
        CreateCharacterDto dto = new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        WorldCharacter result = characterService.transformFromDto(dto, testWorld);
        assertEquals("Lucian Varek", result.getCharacterName());
        assertEquals("Human", result.getCharacterSpecies());
        assertEquals(34, result.getAge());
        assertEquals("Test character", result.getCharacterDescription());
        assertEquals("Eisenmarkian", result.getCharacterNationality());
        assertSame(testWorld, result.getWorld());
    }

    @Test
    void toDto_shouldMapCharacterWithoutExposingWorldEntity() {
        CharacterResponseDto result = characterService.toDto(testCharacter);
        assertEquals(10L, result.characterId());
        assertEquals("Lucian Varek", result.characterName());
        assertEquals("Human", result.characterSpecies());
        assertEquals(34, result.age());
        assertEquals("Test character", result.characterDescription());
        assertEquals("Eisenmarkian", result.characterNationality());
        assertEquals(2L, result.worldId());
    }
}
