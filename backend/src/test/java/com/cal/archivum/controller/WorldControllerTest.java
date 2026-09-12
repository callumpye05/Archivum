package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.security.ArchivumSecurityConfig;
import com.cal.archivum.service.IWorldService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorldController.class)
@Import(ArchivumSecurityConfig.class)
@WithMockUser(
        username = "test_user",
        roles = "USER"
)
class WorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IWorldService worldService;

    private World testWorld;

    @BeforeEach
    void setUp() {

        testWorld = new World();
        testWorld.setWorldId(1L);
        testWorld.setWorldName("Test world");
        testWorld.setWorldDesc(
                "A test for the controller"
        );
    }


    // =========================================================
    // SECURITY
    // =========================================================

    @Test
    @WithAnonymousUser
    void getWorld_shouldReturn401_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/worlds/1")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(worldService);
    }


    // =========================================================
    // GET WORLD
    // =========================================================

    @Test
    void getWorld_shouldReturn200_whenWorldExists()
            throws Exception {

        when(worldService.getWorld(1L))
                .thenReturn(testWorld);

        mockMvc.perform(
                        get("/worlds/1")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.worldId")
                                .value(1L)
                )
                .andExpect(
                        jsonPath("$.worldName")
                                .value("Test world")
                )
                .andExpect(
                        jsonPath("$.worldDesc")
                                .value(
                                        "A test for the controller"
                                )
                );

        verify(worldService)
                .getWorld(1L);
    }

    @Test
    void getWorld_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        when(worldService.getWorld(99L))
                .thenThrow(
                        new WorldNotFound(99L)
                );

        mockMvc.perform(
                        get("/worlds/99")
                )
                .andExpect(status().isNotFound());

        verify(worldService)
                .getWorld(99L);
    }


    // =========================================================
    // GET ALL WORLDS
    // =========================================================

    @Test
    void getAllWorlds_shouldReturn200_whenWorldsAreReturned()
            throws Exception {

        World secondWorld = new World();
        secondWorld.setWorldId(2L);
        secondWorld.setWorldName("Eisenmark");
        secondWorld.setWorldDesc(
                "Another test world"
        );

        when(worldService.getAllWorlds())
                .thenReturn(
                        List.of(
                                testWorld,
                                secondWorld
                        )
                );

        mockMvc.perform(
                        get("/worlds")
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$[0].worldId")
                                .value(1L)
                )
                .andExpect(
                        jsonPath("$[0].worldName")
                                .value("Test world")
                )

                .andExpect(
                        jsonPath("$[1].worldId")
                                .value(2L)
                )
                .andExpect(
                        jsonPath("$[1].worldName")
                                .value("Eisenmark")
                );

        verify(worldService)
                .getAllWorlds();
    }

    @Test
    void getAllWorlds_shouldReturnEmptyArray_whenNoWorldsAreReturned()
            throws Exception {

        when(worldService.getAllWorlds())
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/worlds")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );

        verify(worldService)
                .getAllWorlds();
    }


    // =========================================================
    // CREATE WORLD
    // =========================================================

    @Test
    void createWorld_shouldReturn200_whenRequestIsValid()
            throws Exception {

        CreateWorldDto dto =
                new CreateWorldDto(
                        "Novarii",
                        "Technologically advanced world"
                );

        World createdWorld = new World();
        createdWorld.setWorldId(3L);
        createdWorld.setWorldName("Novarii");
        createdWorld.setWorldDesc(
                "Technologically advanced world"
        );

        when(
                worldService.createWorld(
                        any(CreateWorldDto.class)
                )
        ).thenReturn(createdWorld);

        mockMvc.perform(
                        post("/worlds/create")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(dto)
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.worldId")
                                .value(3L)
                )
                .andExpect(
                        jsonPath("$.worldName")
                                .value("Novarii")
                )
                .andExpect(
                        jsonPath("$.worldDesc")
                                .value(
                                        "Technologically advanced world"
                                )
                );

        verify(worldService)
                .createWorld(
                        any(CreateWorldDto.class)
                );
    }

    @Test
    void createWorld_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        CreateWorldDto invalidDto =
                new CreateWorldDto(
                        "iiiiiiiioopppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                                "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                        "Some description"
                );

        mockMvc.perform(
                        post("/worlds/create")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        invalidDto
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                worldService,
                never()
        ).createWorld(
                any(CreateWorldDto.class)
        );
    }


    // =========================================================
    // UPDATE WORLD
    // =========================================================

    @Test
    void updateWorld_shouldReturn200_whenRequestIsValid()
            throws Exception {

        UpdateWorldDto dto =
                new UpdateWorldDto(
                        "Updated Highberry",
                        "Updated description"
                );

        World updatedWorld = new World();
        updatedWorld.setWorldId(1L);
        updatedWorld.setWorldName(
                "Updated Highberry"
        );
        updatedWorld.setWorldDesc(
                "Updated description"
        );

        when(
                worldService.updateWorld(
                        eq(1L),
                        any(UpdateWorldDto.class)
                )
        ).thenReturn(updatedWorld);

        mockMvc.perform(
                        put("/worlds/1")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(dto)
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.worldId")
                                .value(1L)
                )
                .andExpect(
                        jsonPath("$.worldName")
                                .value(
                                        "Updated Highberry"
                                )
                )
                .andExpect(
                        jsonPath("$.worldDesc")
                                .value(
                                        "Updated description"
                                )
                );

        verify(worldService)
                .updateWorld(
                        eq(1L),
                        any(UpdateWorldDto.class)
                );
    }

    @Test
    void updateWorld_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        UpdateWorldDto dto =
                new UpdateWorldDto(
                        "Updated",
                        "Updated description"
                );

        when(
                worldService.updateWorld(
                        eq(99L),
                        any(UpdateWorldDto.class)
                )
        ).thenThrow(
                new WorldNotFound(99L)
        );

        mockMvc.perform(
                        put("/worlds/99")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(dto)
                                )
                )
                .andExpect(
                        status().isNotFound()
                );

        verify(worldService)
                .updateWorld(
                        eq(99L),
                        any(UpdateWorldDto.class)
                );
    }

    @Test
    void updateWorld_shouldReturn400_whenRequestIsInvalid()
            throws Exception {

        UpdateWorldDto invalidDto =
                new UpdateWorldDto(
                        "iiiiiiiioopppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                                "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                        ""
                );

        mockMvc.perform(
                        put("/worlds/1")
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        invalidDto
                                                )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verify(
                worldService,
                never()
        ).updateWorld(
                anyLong(),
                any(UpdateWorldDto.class)
        );
    }


    // =========================================================
    // DELETE WORLD
    // =========================================================

    @Test
    void deleteWorld_shouldReturn200_whenWorldExists()
            throws Exception {

        doNothing()
                .when(worldService)
                .deleteWorld(1L);

        mockMvc.perform(
                        delete("/worlds/1")
                )
                .andExpect(
                        status().isOk()
                );

        verify(worldService)
                .deleteWorld(1L);
    }

    @Test
    void deleteWorld_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        doThrow(
                new WorldNotFound(99L)
        )
                .when(worldService)
                .deleteWorld(99L);

        mockMvc.perform(
                        delete("/worlds/99")
                )
                .andExpect(
                        status().isNotFound()
                );

        verify(worldService)
                .deleteWorld(99L);
    }
}