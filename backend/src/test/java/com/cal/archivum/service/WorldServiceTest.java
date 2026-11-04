package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.dto.impl.WorldResponseDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.impl.WorldService;
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
class WorldServiceTest {

    @Mock private WorldRepository worldRepo;
    @Mock private IUserService userService;
    @Mock private CharacterRepository characterRepo;
    @Mock private LocationRepository locationRepo;

    private WorldService worldService;
    private User testUser;
    private World testWorld;

    @BeforeEach
    void setUp() {
        worldService=new WorldService(worldRepo, userService, characterRepo, locationRepo);
        testUser=new User();
        testUser.setUserName("test_user");

        testWorld=new World();
        testWorld.setWorldId(1L);
        testWorld.setWorldName("Highberry");
        testWorld.setWorldDesc("A vast fortified bastion that survived the collapse.");
        testWorld.setOwner(testUser);
    }

    // =========================================================
    // GET WORLD
    // =========================================================

    @Test
    void getWorld_shouldReturnWorldResponseDto_whenOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(1L, testUser)).thenReturn(Optional.of(testWorld));

        WorldResponseDto result=worldService.getWorld(1L);

        assertNotNull(result);
        assertEquals(1L, result.worldId());
        assertEquals("Highberry", result.worldName());
        assertEquals("A vast fortified bastion that survived the collapse.", result.worldDesc());
        verify(worldRepo).findByWorldIdAndOwner(1L, testUser);
    }

    @Test
    void getWorld_shouldThrow_whenWorldNotAccessibleToCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(WorldNotFound.class, () -> worldService.getWorld(99L));
        verify(worldRepo).findByWorldIdAndOwner(99L, testUser);
    }

    // =========================================================
    // GET ALL WORLDS
    // =========================================================

    @Test
    void getAllWorlds_shouldReturnOnlyWorldsOwnedByCurrentUser() {
        World secondWorld=new World();
        secondWorld.setWorldId(2L);
        secondWorld.setWorldName("Eisenmark");
        secondWorld.setOwner(testUser);
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findAllByOwner(testUser)).thenReturn(List.of(testWorld, secondWorld));

        List<WorldResponseDto> result=worldService.getAllWorlds();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).worldId());
        assertEquals("Highberry", result.get(0).worldName());
        assertEquals(2L, result.get(1).worldId());
        assertEquals("Eisenmark", result.get(1).worldName());
        verify(worldRepo).findAllByOwner(testUser);
    }

    @Test
    void getAllWorlds_shouldReturnEmptyList_whenCurrentUserOwnsNoWorlds() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findAllByOwner(testUser)).thenReturn(List.of());

        List<WorldResponseDto> result=worldService.getAllWorlds();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(worldRepo).findAllByOwner(testUser);
    }

    // =========================================================
    // CREATE WORLD
    // =========================================================

    @Test
    void createWorld_shouldAssignCurrentUserAsOwnerAndReturnSavedWorldDto() {
        CreateWorldDto dto=new CreateWorldDto("Highberry", "A vast fortified bastion.");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.save(any(World.class))).thenAnswer(invocation -> {
            World world=invocation.getArgument(0);
            world.setWorldId(1L);
            return world;
        });

        WorldResponseDto result=worldService.createWorld(dto);

        assertNotNull(result);
        assertEquals(1L, result.worldId());
        assertEquals("Highberry", result.worldName());
        assertEquals("A vast fortified bastion.", result.worldDesc());
        ArgumentCaptor<World> captor=ArgumentCaptor.forClass(World.class);
        verify(worldRepo).save(captor.capture());
        assertSame(testUser, captor.getValue().getOwner());
        verify(userService).getCurrentUser();
    }

    @Test
    void createWorld_shouldMapRepositoryReturnedEntityRatherThanOriginalReference() {
        CreateWorldDto dto=new CreateWorldDto("Highberry", "A vast fortified bastion.");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.save(any(World.class))).thenAnswer(invocation -> {
            World saved=new World();
            saved.setWorldId(42L);
            saved.setWorldName("Highberry");
            saved.setWorldDesc("A vast fortified bastion.");
            saved.setOwner(testUser);
            return saved;
        });

        WorldResponseDto result=worldService.createWorld(dto);

        assertEquals(42L, result.worldId());
        assertEquals("Highberry", result.worldName());
        verify(worldRepo).save(any(World.class));
    }

    // =========================================================
    // UPDATE WORLD
    // =========================================================

    @Test
    void updateWorld_shouldUpdateProvidedFields_whenOwnedByCurrentUser() {
        UpdateWorldDto dto=new UpdateWorldDto("Highberry Prime", "An updated description.");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(1L, testUser)).thenReturn(Optional.of(testWorld));
        when(worldRepo.save(testWorld)).thenReturn(testWorld);

        WorldResponseDto result=worldService.updateWorld(1L, dto);

        assertEquals(1L, result.worldId());
        assertEquals("Highberry Prime", result.worldName());
        assertEquals("An updated description.", result.worldDesc());
        assertSame(testUser, testWorld.getOwner());
        verify(worldRepo).findByWorldIdAndOwner(1L, testUser);
        verify(worldRepo).save(testWorld);
    }

    @Test
    void updateWorld_shouldOnlyUpdateNonNullFields() {
        UpdateWorldDto dto=new UpdateWorldDto(null, "Only the description changed.");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(1L, testUser)).thenReturn(Optional.of(testWorld));
        when(worldRepo.save(testWorld)).thenReturn(testWorld);

        WorldResponseDto result=worldService.updateWorld(1L, dto);

        assertEquals("Highberry", result.worldName());
        assertEquals("Only the description changed.", result.worldDesc());
        verify(worldRepo).save(testWorld);
    }

    @Test
    void updateWorld_shouldThrow_whenWorldNotOwnedByCurrentUser() {
        UpdateWorldDto dto=new UpdateWorldDto("Updated", "Updated description");
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(WorldNotFound.class, () -> worldService.updateWorld(99L, dto));
        verify(worldRepo, never()).save(any(World.class));
    }

    // =========================================================
    // DELETE WORLD
    // =========================================================

    @Test
    void deleteWorld_shouldDelete_whenWorldOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(1L, testUser)).thenReturn(Optional.of(testWorld));

        worldService.deleteWorld(1L);

        verify(worldRepo).findByWorldIdAndOwner(1L, testUser);
        verify(characterRepo).deleteAllByWorld(testWorld);
        verify(locationRepo).deleteAllByWorld(testWorld);
        verify(worldRepo).delete(testWorld);
    }

    @Test
    void deleteWorld_shouldThrow_whenWorldNotOwnedByCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(worldRepo.findByWorldIdAndOwner(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(WorldNotFound.class, () -> worldService.deleteWorld(99L));
        verify(worldRepo, never()).delete(any(World.class));
        verifyNoInteractions(characterRepo, locationRepo);
    }

    // =========================================================
    // DTO MAPPING
    // =========================================================

    @Test
    void transformFromDto_shouldCreateWorldFromDto_withoutAssigningOwner() {
        CreateWorldDto dto=new CreateWorldDto("Novaris", "A technologically advanced world.");

        World result=worldService.transformFromDto(dto);

        assertNotNull(result);
        assertEquals("Novaris", result.getWorldName());
        assertEquals("A technologically advanced world.", result.getWorldDesc());
        assertNull(result.getOwner());
    }

    @Test
    void toDto_shouldExposeOnlyWorldResponseFields() {
        WorldResponseDto result=worldService.toDto(testWorld);

        assertEquals(1L, result.worldId());
        assertEquals("Highberry", result.worldName());
        assertEquals("A vast fortified bastion that survived the collapse.", result.worldDesc());
    }
}
