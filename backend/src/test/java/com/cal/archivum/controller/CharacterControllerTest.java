package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.exception.CharacterNotFound;
import com.cal.archivum.exception.CharacterNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.service.ICharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CharacterController.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICharacterService characterService;
    private World testWorld;
    private WorldCharacter testCharacter;

    @BeforeEach
    void setup() {

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");
        testWorld.setWorldDesc("Industrial world");
        testCharacter = new WorldCharacter();
        testCharacter.setCharacterId(10L);
        testCharacter.setCharacterName("Lucian Varek");
        testCharacter.setCharacterSpecies("Human");
        testCharacter.setAge(34);
        testCharacter.setCharacterDescription("Test character");
        testCharacter.setCharacterNationality("Eisenmarkian");
        testCharacter.setWorld(testWorld);
    }




    @Test
    void getCharacter_shouldReturn200_whenCharacterExists() throws Exception {
        when(characterService.getCharacter(10L)).thenReturn(testCharacter);
        mockMvc.perform(get("/characters/10")).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Varek")).andExpect(jsonPath("$.characterSpecies").value("Human")).andExpect(jsonPath("$.age").value(34));
        verify(characterService).getCharacter(10L);
    }

    @Test
    void getCharacter_shouldReturn404_whenCharacterDoesNotExist() throws Exception {

        when(characterService.getCharacter(99L)).thenThrow(new CharacterNotFound(99L));
        mockMvc.perform(get("/characters/99")).andExpect(status().isNotFound());
        verify(characterService).getCharacter(99L);
    }
    // GET ALL CHARACTERS FROM WORLD

    @Test
    void getAllCharactersFromWorld_shouldReturn200_whenWorldExists() throws Exception {

        when(characterService.getAllCharactersFromWorld(2L)).thenReturn(List.of(testCharacter));
        mockMvc.perform(get("/worlds/2/characters")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].characterId").value(10L)).andExpect(jsonPath("$[0].characterName").value("Lucian Varek"));
        verify(characterService).getAllCharactersFromWorld(2L);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturn200AndEmptyList_whenWorldHasNoCharacters()
            throws Exception {
        when(characterService.getAllCharactersFromWorld(2L)).thenReturn(List.of());
        mockMvc.perform(get("/worlds/2/characters")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        verify(characterService).getAllCharactersFromWorld(2L);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturn404_whenWorldDoesNotExist() throws Exception {
        when(characterService.getAllCharactersFromWorld(99L)).thenThrow(new WorldNotFound(99L));
        mockMvc.perform(get("/worlds/99/characters")).andExpect(status().isNotFound());
        verify(characterService).getAllCharactersFromWorld(99L);
    }
    // GET CHARACTER FROM SPECIFIC WORLD


    @Test
    void getCharacterByWorld_shouldReturn200_whenCharacterBelongsToWorld() throws Exception {
        when(characterService.getCharacterByWorldId(2L, 10L)).thenReturn(testCharacter);

        mockMvc.perform(get("/worlds/2/characters/10")).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Varek"));

        verify(characterService).getCharacterByWorldId(2L, 10L);
    }

    @Test
    void getCharacterByWorld_shouldReturn404_whenCharacterDoesNotBelongToWorld()
            throws Exception {

        when(characterService.getCharacterByWorldId(3L, 10L)).thenThrow(new CharacterNotFoundByWorld(10L, 3L));
        mockMvc.perform(get("/worlds/3/characters/10")).andExpect(status().isNotFound());
        verify(characterService).getCharacterByWorldId(3L, 10L);
    }
    // CREATE CHARACTER
    @Test
    void createCharacter_shouldReturn200_whenRequestIsValid()
            throws Exception {

        CreateCharacterDto dto = new CreateCharacterDto(
                "Lucian Varek",
                "Human",
                34,
                "Test character",
                "Eisenmarkian"
        );

        when(characterService.createCharacter(
                any(CreateCharacterDto.class),
                eq(2L)
        )).thenReturn(testCharacter);

        mockMvc.perform(
                        post("/worlds/2/characters")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characterId").value(10L))
                .andExpect(jsonPath("$.characterName").value("Lucian Varek"));

        verify(characterService)
                .createCharacter(any(CreateCharacterDto.class), eq(2L));
    }

    @Test
    void createCharacter_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        CreateCharacterDto dto = new CreateCharacterDto(
                "Lucian Varek",
                "Human",
                34,
                "Test character",
                "Eisenmarkian"
        );

        when(characterService.createCharacter(
                any(CreateCharacterDto.class),
                eq(99L)
        )).thenThrow(new WorldNotFound(99L));

        mockMvc.perform(
                        post("/worlds/99/characters")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }


    // =========================================================
    // UPDATE CHARACTER
    // =========================================================

    @Test
    void updateCharacter_shouldReturn200_whenRequestIsValid()
            throws Exception {

        UpdateCharacterDto dto = new UpdateCharacterDto(
                "Lucian Updated",
                "Human",
                35,
                "Updated description",
                "Eisenmarkian"
        );

        WorldCharacter updated = new WorldCharacter();
        updated.setCharacterId(10L);
        updated.setCharacterName("Lucian Updated");
        updated.setCharacterSpecies("Human");
        updated.setAge(35);
        updated.setCharacterDescription("Updated description");
        updated.setCharacterNationality("Eisenmarkian");
        updated.setWorld(testWorld);

        when(characterService.updateCharacter(
                eq(10L),
                any(UpdateCharacterDto.class)
        )).thenReturn(updated);

        mockMvc.perform(
                        put("/characters/10")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characterId").value(10L))
                .andExpect(jsonPath("$.characterName").value("Lucian Updated"))
                .andExpect(jsonPath("$.age").value(35));

        verify(characterService)
                .updateCharacter(eq(10L), any(UpdateCharacterDto.class));
    }

    @Test
    void updateCharacter_shouldReturn404_whenCharacterDoesNotExist()
            throws Exception {

        UpdateCharacterDto dto = new UpdateCharacterDto(
                "Updated",
                null,
                null,
                null,
                null
        );

        when(characterService.updateCharacter(
                eq(99L),
                any(UpdateCharacterDto.class)
        )).thenThrow(new CharacterNotFound(99L));

        mockMvc.perform(
                        put("/characters/99")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());
    }


    // DELETE CHARACTER
    @Test
    void deleteCharacter_shouldReturn200_whenCharacterExists()
            throws Exception {

        doNothing()
                .when(characterService)
                .deleteCharacter(10L);

        mockMvc.perform(delete("/characters/10"))
                .andExpect(status().isOk());

        verify(characterService).deleteCharacter(10L);
    }

    @Test
    void deleteCharacter_shouldReturn404_whenCharacterDoesNotExist()
            throws Exception {

        doThrow(new CharacterNotFound(99L))
                .when(characterService)
                .deleteCharacter(99L);

        mockMvc.perform(delete("/characters/99"))
                .andExpect(status().isNotFound());

        verify(characterService).deleteCharacter(99L);
    }
}