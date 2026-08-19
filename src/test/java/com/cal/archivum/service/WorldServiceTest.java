package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.impl.WorldService;
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
class WorldServiceTest {

    @Mock
    private WorldRepository worldRepo;

    private WorldService worldService;

    private World testWorld;

    @BeforeEach
    void setUp() {

        worldService = new WorldService(worldRepo);

        testWorld = new World();
        testWorld.setWorldId(1L);
        testWorld.setWorldName("Highberry");
        testWorld.setWorldDesc(
                "A vast fortified bastion that survived the collapse."
        );
    }


    // =========================================================
    // GET WORLD
    // =========================================================

    @Test
    void getWorld_shouldReturnWorld_whenWorldExists() {

        when(worldRepo.findById(1L))
                .thenReturn(Optional.of(testWorld));

        World result = worldService.getWorld(1L);

        assertNotNull(result);
        assertEquals(1L, result.getWorldId());
        assertEquals("Highberry", result.getWorldName());

        verify(worldRepo).findById(1L);
    }

    @Test
    void getWorld_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        when(worldRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> worldService.getWorld(99L)
        );

        verify(worldRepo).findById(99L);
    }


    // =========================================================
    // GET ALL WORLDS
    // =========================================================

    @Test
    void getAllWorlds_shouldReturnAllWorlds() {

        World secondWorld = new World();
        secondWorld.setWorldId(2L);
        secondWorld.setWorldName("Eisenmark");

        when(worldRepo.findAll())
                .thenReturn(List.of(testWorld, secondWorld));

        List<World> result = worldService.getAllWorlds();

        assertEquals(2, result.size());
        assertEquals("Highberry", result.get(0).getWorldName());
        assertEquals("Eisenmark", result.get(1).getWorldName());

        verify(worldRepo).findAll();
    }

    @Test
    void getAllWorlds_shouldReturnEmptyList_whenNoWorldsExist() {

        when(worldRepo.findAll())
                .thenReturn(List.of());

        List<World> result = worldService.getAllWorlds();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(worldRepo).findAll();
    }


    // =========================================================
    // CREATE WORLD
    // =========================================================

    @Test
    void createWorld_shouldSaveAndReturnWorld() {

        CreateWorldDto dto = new CreateWorldDto(
                "Highberry",
                "A vast fortified bastion."
        );

        when(worldRepo.save(any(World.class)))
                .thenAnswer(invocation -> {

                    World world = invocation.getArgument(0);
                    world.setWorldId(1L);

                    return world;
                });

        World result = worldService.createWorld(dto);

        assertNotNull(result);
        assertEquals(1L, result.getWorldId());
        assertEquals("Highberry", result.getWorldName());
        assertEquals(
                "A vast fortified bastion.",
                result.getWorldDesc()
        );

        verify(worldRepo).save(any(World.class));
    }


    // =========================================================
    // UPDATE WORLD
    // =========================================================

    @Test
    void updateWorld_shouldUpdateProvidedFields() {

        UpdateWorldDto dto = new UpdateWorldDto(
                "Highberry Prime",
                "An updated description."
        );

        when(worldRepo.findById(1L))
                .thenReturn(Optional.of(testWorld));

        when(worldRepo.save(testWorld))
                .thenReturn(testWorld);

        World result = worldService.updateWorld(1L, dto);

        assertEquals(
                "Highberry Prime",
                result.getWorldName()
        );

        assertEquals(
                "An updated description.",
                result.getWorldDesc()
        );

        verify(worldRepo).findById(1L);
        verify(worldRepo).save(testWorld);
    }

    @Test
    void updateWorld_shouldOnlyUpdateNonNullFields() {

        UpdateWorldDto dto = new UpdateWorldDto(
                null,
                "Only the description changed."
        );

        when(worldRepo.findById(1L))
                .thenReturn(Optional.of(testWorld));

        when(worldRepo.save(testWorld))
                .thenReturn(testWorld);

        World result = worldService.updateWorld(1L, dto);

        assertEquals(
                "Highberry",
                result.getWorldName()
        );

        assertEquals(
                "Only the description changed.",
                result.getWorldDesc()
        );

        verify(worldRepo).save(testWorld);
    }

    @Test
    void updateWorld_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        UpdateWorldDto dto = new UpdateWorldDto(
                "Updated",
                "Updated description"
        );

        when(worldRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> worldService.updateWorld(99L, dto)
        );

        verify(worldRepo, never()).save(any());
    }


    // =========================================================
    // DELETE WORLD
    // =========================================================

    @Test
    void deleteWorld_shouldDeleteWorld_whenWorldExists() {

        when(worldRepo.findById(1L))
                .thenReturn(Optional.of(testWorld));

        worldService.deleteWorld(1L);

        verify(worldRepo).findById(1L);
        verify(worldRepo).deleteById(1L);
    }

    @Test
    void deleteWorld_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        when(worldRepo.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> worldService.deleteWorld(99L)
        );

        verify(worldRepo, never()).deleteById(anyLong());
    }


    // =========================================================
    // TRANSFORM DTO
    // =========================================================

    @Test
    void transformFromDto_shouldCreateWorldFromDto() {

        CreateWorldDto dto = new CreateWorldDto(
                "Novaris",
                "A technologically advanced world."
        );

        World result = worldService.transformFromDto(dto);

        assertNotNull(result);

        assertEquals(
                "Novaris",
                result.getWorldName()
        );

        assertEquals(
                "A technologically advanced world.",
                result.getWorldDesc()
        );
    }
}
