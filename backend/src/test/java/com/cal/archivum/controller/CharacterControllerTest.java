package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.exception.CharacterNotFound;
import com.cal.archivum.exception.CharacterNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.security.ArchivumSecurityConfig;
import com.cal.archivum.service.ICharacterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CharacterController.class)
@Import(ArchivumSecurityConfig.class)
@WithMockUser(
        username = "test_user",
        roles = "USER"
)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICharacterService characterService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private World testWorld;
    private WorldCharacter testCharacter;


    @BeforeEach
    void setUp() {

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


    // =========================================================
    // SECURITY
    // =========================================================

    @Test
    @WithAnonymousUser
    void getCharacter_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/characters/10").with(csrf())).andExpect(status().isUnauthorized());
        verifyNoInteractions(characterService);
    }


    // =========================================================
    // GET CHARACTER
    // =========================================================

    @Test
    void getCharacter_shouldReturn200_whenCharacterExists() throws Exception {
        when(characterService.getCharacter(10L)).thenReturn(testCharacter);
        mockMvc.perform(get("/characters/10")).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Varek")).andExpect(jsonPath("$.characterSpecies").value("Human")).andExpect(jsonPath("$.age").value(34)).andExpect(jsonPath("$.characterDescription").value("Test character")).andExpect(jsonPath("$.characterNationality").value("Eisenmarkian"));
        verify(characterService).getCharacter(10L);
    }

    @Test
    void getCharacter_shouldReturn404_whenCharacterDoesNotExist() throws Exception {
        when(characterService.getCharacter(99L)).thenThrow(new CharacterNotFound(99L));
        mockMvc.perform(get("/characters/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/characters/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).getCharacter(99L);
    }


    // =========================================================
    // GET ALL CHARACTERS FROM WORLD
    // =========================================================

    @Test
    void getAllCharactersFromWorld_shouldReturn200_whenWorldExists() throws Exception {
        when(characterService.getAllCharactersFromWorld(2L)).thenReturn(List.of(testCharacter));
        mockMvc.perform(get("/worlds/2/characters")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].characterId").value(10L)).andExpect(jsonPath("$[0].characterName").value("Lucian Varek"));
        verify(characterService).getAllCharactersFromWorld(2L);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturn200AndEmptyList_whenWorldHasNoCharacters() throws Exception {
        when(characterService.getAllCharactersFromWorld(2L)).thenReturn(List.of());
        mockMvc.perform(get("/worlds/2/characters")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        verify(characterService).getAllCharactersFromWorld(2L);
    }

    @Test
    void getAllCharactersFromWorld_shouldReturn404_whenWorldDoesNotExist() throws Exception {
        when(characterService.getAllCharactersFromWorld(99L)).thenThrow(new WorldNotFound(99L));
        mockMvc.perform(get("/worlds/99/characters")).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/99/characters")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).getAllCharactersFromWorld(99L);
    }


    // =========================================================
    // GET CHARACTER FROM SPECIFIC WORLD
    // =========================================================

    @Test
    void getCharacterByWorld_shouldReturn200_whenCharacterBelongsToWorld() throws Exception {
        when(characterService.getCharacterByWorldId(2L, 10L)).thenReturn(testCharacter);
        mockMvc.perform(get("/worlds/2/characters/10")).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Varek")).andExpect(jsonPath("$.characterSpecies").value("Human"));
        verify(characterService).getCharacterByWorldId(2L, 10L);
    }

    @Test
    void getCharacterByWorld_shouldReturn404_whenCharacterDoesNotBelongToWorld() throws Exception {
        when(characterService.getCharacterByWorldId(3L, 10L)).thenThrow(new CharacterNotFoundByWorld(10L, 3L));
        mockMvc.perform(get("/worlds/3/characters/10")).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/3/characters/10")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).getCharacterByWorldId(3L, 10L);
    }


    // =========================================================
    // CREATE CHARACTER
    // =========================================================

    @Test
    void createCharacter_shouldReturn200_whenRequestIsValid() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        when(characterService.createCharacter(any(CreateCharacterDto.class), eq(2L))).thenReturn(testCharacter);
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Varek")).andExpect(jsonPath("$.characterSpecies").value("Human")).andExpect(jsonPath("$.age").value(34));
        verify(characterService).createCharacter(any(CreateCharacterDto.class), eq(2L));
    }

    @Test
    void createCharacter_shouldReturn400_whenAgeIsNegative() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("Lucian Varek", "Human", -5, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage").value("Character age cannot be negative")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }


    @Test
    void createCharacter_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("   ", "Human", 34, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage").value("Character name is required")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void createCharacter_shouldReturn400_whenSpeciesIsBlank() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("Lucian Varek", "", 34, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage").value("Character species is required")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void createCharacter_shouldReturn400_whenAgeIsNull() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("Lucian Varek", "Human", null, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage").value("Character age is required")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void createCharacter_shouldReturn400_whenNameExceedsMaximumLength() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("A".repeat(101), "Human", 34, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage").value("Character name must not exceed 100 characters")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void createCharacter_shouldReturn400_whenMultipleFieldsAreInvalid() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("", "Human", -5, "Test character", "Eisenmarkian");
        mockMvc.perform(post("/worlds/2/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/characters")).andExpect(jsonPath("$.errorMessage", containsString("Character name is required"))).andExpect(jsonPath("$.errorMessage", containsString("Character age cannot be negative"))).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void createCharacter_shouldReturn404_whenWorldDoesNotExist() throws Exception {
        CreateCharacterDto dto=new CreateCharacterDto("Lucian Varek", "Human", 34, "Test character", "Eisenmarkian");
        when(characterService.createCharacter(any(CreateCharacterDto.class), eq(99L))).thenThrow(new WorldNotFound(99L));
        mockMvc.perform(post("/worlds/99/characters").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/99/characters")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).createCharacter(any(CreateCharacterDto.class), eq(99L));
    }


    // =========================================================
    // UPDATE CHARACTER
    // =========================================================

    @Test
    void updateCharacter_shouldReturn200_whenRequestIsValid() throws Exception {
        UpdateCharacterDto dto=new UpdateCharacterDto("Lucian Updated", "Human", 35, "Updated description", "Eisenmarkian");
        WorldCharacter updated=new WorldCharacter();
        updated.setCharacterId(10L);
        updated.setCharacterName("Lucian Updated");
        updated.setCharacterSpecies("Human");
        updated.setAge(35);
        updated.setCharacterDescription("Updated description");
        updated.setCharacterNationality("Eisenmarkian");
        updated.setWorld(testWorld);
        when(characterService.updateCharacter(eq(10L), any(UpdateCharacterDto.class))).thenReturn(updated);
        mockMvc.perform(put("/characters/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(10L)).andExpect(jsonPath("$.characterName").value("Lucian Updated")).andExpect(jsonPath("$.characterSpecies").value("Human")).andExpect(jsonPath("$.age").value(35)).andExpect(jsonPath("$.characterDescription").value("Updated description"));
        verify(characterService).updateCharacter(eq(10L), any(UpdateCharacterDto.class));
    }

    @Test
    void updateCharacter_shouldReturn400_whenAgeIsNegative() throws Exception {
        UpdateCharacterDto dto=new UpdateCharacterDto(null, null, -5, null, null);
        mockMvc.perform(put("/characters/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/characters/10")).andExpect(jsonPath("$.errorMessage").value("Character age cannot be negative")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void updateCharacter_shouldReturn400_whenDescriptionExceedsMaximumLength() throws Exception {
        UpdateCharacterDto dto=new UpdateCharacterDto(null, null, null, "A".repeat(501), null);
        mockMvc.perform(put("/characters/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/characters/10")).andExpect(jsonPath("$.errorMessage").value("Character description must not exceed 500 characters")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(characterService);
    }

    @Test
    void updateCharacter_shouldReturn404_whenCharacterDoesNotExist() throws Exception {
        UpdateCharacterDto dto=new UpdateCharacterDto("Updated", null, null, null, null);
        when(characterService.updateCharacter(eq(99L), any(UpdateCharacterDto.class))).thenThrow(new CharacterNotFound(99L));
        mockMvc.perform(put("/characters/99").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/characters/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).updateCharacter(eq(99L), any(UpdateCharacterDto.class));
    }


    // =========================================================
    // DELETE CHARACTER
    // =========================================================

    @Test
    void deleteCharacter_shouldReturn200_whenCharacterExists() throws Exception {
        doNothing().when(characterService).deleteCharacter(10L);
        mockMvc.perform(delete("/characters/10").with(csrf())).andExpect(status().isOk());
        verify(characterService).deleteCharacter(10L);
    }

    @Test
    void deleteCharacter_shouldReturn404_whenCharacterDoesNotExist() throws Exception {
        doThrow(new CharacterNotFound(99L)).when(characterService).deleteCharacter(99L);
        mockMvc.perform(delete("/characters/99").with(csrf())).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/characters/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(characterService).deleteCharacter(99L);
    }
}

