package com.cal.archivum.controller;


import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.service.IWorldService;
import com.cal.archivum.service.impl.WorldService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorldController.class)
class WorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IWorldService worldService;


    private World testWorld;


    @BeforeEach
    void setup() {
        testWorld = new World();
        testWorld.setWorldId(1L);
        testWorld.setWorldName("Test world");
        testWorld.setWorldDesc("A test for the controller"
        );
    }


    @Test
    void getWorld_shouldReturn200_whenWorldExists() throws Exception {

        when(worldService.getWorld(1L))
                .thenReturn(testWorld);

        mockMvc.perform(get("/worlds/1"))
                .andExpect(status().isOk())
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.worldId")
                                .value(1L)
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.worldName")
                                .value(testWorld.getWorldName())
                )
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .jsonPath("$.worldDesc")
                                .value(testWorld.getWorldDesc())
                );

        verify(worldService).getWorld(1L);
    }

    @Test
    void getWorld_shouldReturn404_whenWorldDoesntExist() throws Exception {
        when(worldService.getWorld(2L)).thenThrow(new WorldNotFound(2L));

        mockMvc.perform(get("/worlds/2")).andExpect(status().isNotFound());

        verify(worldService).getWorld(2L);
    }


    @Test
    void getAllWorlds_shouldReturn200_whenWorldsAreReturned() throws Exception {
        when(worldService.getAllWorlds()).thenReturn(List.of(testWorld));

        mockMvc.perform(get("/worlds")).andExpect(status().isOk());
    }

    @Test
    void createWorld_shouldReturn200_whenRequestIsValid() throws Exception {

        CreateWorldDto dto = new CreateWorldDto(
                "Novarii",
                "Technologically advanced world"
        );

        when(worldService.createWorld(any(CreateWorldDto.class)))
                .thenReturn(testWorld);

        mockMvc.perform(post("/worlds/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }


    @Test
    void createWorld_shouldReturn400_whenRequestIsInvalid() throws Exception {

        CreateWorldDto invalidDto = new CreateWorldDto(
                "iiiiiiiioopppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                        "",
                "Some description"
        );

        mockMvc.perform(
                        post("/worlds/create")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(invalidDto))
                )
                .andExpect(status().isBadRequest());

        verify(worldService, never())
                .createWorld(any(CreateWorldDto.class));
    }



    @Test
    void updateWorld_shouldReturn200_whenRequestIsValid() throws Exception {

        UpdateWorldDto dto = new UpdateWorldDto(
                "Updated Highberry",
                "Updated description"
        );

        World updatedWorld = new World();
        updatedWorld.setWorldId(1L);
        updatedWorld.setWorldName("Updated Highberry");
        updatedWorld.setWorldDesc("Updated description");

        when(worldService.updateWorld(eq(1L), any(UpdateWorldDto.class)))
                .thenReturn(updatedWorld);

        mockMvc.perform(
                        put("/worlds/1")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.worldId").value(1L))
                .andExpect(jsonPath("$.worldName").value("Updated Highberry"))
                .andExpect(jsonPath("$.worldDesc").value("Updated description"));

        verify(worldService)
                .updateWorld(eq(1L), any(UpdateWorldDto.class));
    }


    @Test
    void updateWorld_shouldReturn404_whenWorldDoesNotExist() throws Exception {

        UpdateWorldDto dto = new UpdateWorldDto(
                "Updated",
                "Updated description"
        );

        when(worldService.updateWorld(eq(99L), any(UpdateWorldDto.class)))
                .thenThrow(new WorldNotFound(99L));

        mockMvc.perform(
                        put("/worlds/99")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());

        verify(worldService)
                .updateWorld(eq(99L), any(UpdateWorldDto.class));
    }


    @Test
    void updateWorld_shouldReturn400_whenRequestIsInvalid() throws Exception {

        UpdateWorldDto invalidDto = new UpdateWorldDto(
                "iiiiiiiioopppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                        "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp" +
                        "",
                ""
        );

        mockMvc.perform(
                        put("/worlds/1")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(invalidDto))
                )
                .andExpect(status().isBadRequest());

        verify(worldService, never())
                .updateWorld(anyLong(), any(UpdateWorldDto.class));
    }


    @Test
    void deleteWorld_shouldReturn200_whenWorldExists() throws Exception {

        doNothing()
                .when(worldService)
                .deleteWorld(1L);

        mockMvc.perform(delete("/worlds/1"))
                .andExpect(status().isOk());

        verify(worldService).deleteWorld(1L);
    }

    @Test
    void deleteWorld_shouldReturn404_whenWorldDoesNotExist() throws Exception {

        doThrow(new WorldNotFound(99L))
                .when(worldService)
                .deleteWorld(99L);

        mockMvc.perform(delete("/worlds/99"))
                .andExpect(status().isNotFound());

        verify(worldService).deleteWorld(99L);
    }




}
