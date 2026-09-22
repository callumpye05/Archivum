package com.cal.archivum;

import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.UserRepository;
import com.cal.archivum.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static com.cal.archivum.enums.LocationType.CITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ownershiptest",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class ResourceOwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorldRepository worldRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private World worldA;
    private UpdateWorldDto updateWorld;

    private Location locationA;
    private WorldCharacter characterA;
    private CreateWorldDto createWorld;

    private World worldC;


    @BeforeEach
    void setup() {

        characterRepository.deleteAll();
        locationRepository.deleteAll();
        worldRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setUserName("userA");
        userA.setEmail("userA@test.com");

        userA.setUserHashedPassword(passwordEncoder.encode("password"));
        userRepository.save(userA);

        worldA = new World();
        worldA.setWorldDesc("test world");
        worldA.setWorldName("World A");
        worldA.setOwner(userA);
        worldRepository.save(worldA);

        worldC = new World();
        worldC.setWorldDesc("test world");
        worldC.setWorldName("World C");
        worldC.setOwner(userA);
        worldRepository.save(worldC);

        locationA = new Location();
        locationA.setLocationName("test location");
        locationA.setLocationDescription("test");
        locationA.setLocationType(CITY);
        locationA.setWorld(worldA);
        locationRepository.save(locationA);

        characterA = new WorldCharacter();
        characterA.setCharacterName("test");
        characterA.setCharacterDescription("test character");
        characterA.setAge(19);
        characterA.setCharacterNationality("french");
        characterA.setCharacterSpecies("human");
        characterA.setWorld(worldA);

        characterRepository.save(characterA);


        userB = new User();
        userB.setUserName("userB");
        userB.setEmail("userB@test.com");

        userB.setUserHashedPassword(passwordEncoder.encode("password"));
        userRepository.save(userB);
        updateWorld = new UpdateWorldDto("NewName" , null);
        createWorld = new CreateWorldDto("NewWorld", "NewDescription");


    }


    @Test
    @WithMockUser(username = "userA", roles = "USER")
    void authenticatedUser_GettingTheirWorld_ShouldReturn200()
            throws Exception {

        mockMvc.perform(get("/worlds/" + worldA.getWorldId())).andExpect(status().isOk()).andExpect(jsonPath("$.worldId").value(worldA.getWorldId()))
                .andExpect(jsonPath("$.worldName").value("World A"));
    }

    @Test
    @WithMockUser(username = "userB" , roles = "USER")
    void authenticatedUser_gettingSomeoneElsesWorld_Should_Return404() throws Exception {
        mockMvc.perform(get("/worlds/" + worldA.getWorldId())).andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "userA", roles = "USER")
    void authenticatedUser_GettingTheirCharacter_ShouldReturn200()
            throws Exception {

        mockMvc.perform(get("/characters/" + characterA.getCharacterId())).andExpect(status().isOk()).andExpect(jsonPath("$.characterId").value(characterA.getCharacterId()))
                .andExpect(jsonPath("$.characterName").value("test")).andExpect(jsonPath("$.characterDescription").value("test character")).andExpect(jsonPath("$.age")
                        .value(19)).andExpect(jsonPath("$.characterNationality").value("french")).andExpect(jsonPath("$.characterSpecies").value("human"));
    }

    @Test
    @WithMockUser(username = "userA", roles = "USER")
    void authenticatedUser_GettingTheirLocation_ShouldReturn200()
            throws Exception {

        mockMvc.perform(get("/locations/" + locationA.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(locationA.getId())).andExpect(jsonPath("$.locationName").value("test location")).andExpect(jsonPath("$.locationDescription").value("test"))
                .andExpect(jsonPath("$.locationType").value("CITY"));
    }

    @Test
    @WithMockUser(username = "userB" , roles = "USER")
    void authenticatedUser_gettingSomeoneElsesCharacter_Should_Return404() throws Exception {
        mockMvc.perform(get("/characters/" + characterA.getCharacterId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "userB" , roles = "USER")
    void authenticatedUser_gettingSomeoneElsesLocation_Should_Return404() throws Exception {
        mockMvc.perform(get("/locations/" + locationA.getId())).andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "userA" , roles = "USER")
    void authenticated_User_ModifyingTheirWorld_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/worlds/" + worldA.getWorldId()).contentType("application/json").content(objectMapper.writeValueAsString(updateWorld))).andExpect(status().isOk()).andExpect(jsonPath("$.worldId").value(worldA.getWorldId()))
                .andExpect(jsonPath("$.worldName").value("NewName"));

    }

    @Test
    @WithMockUser(username = "userB" , roles ="USER")
    void authenticated_User_ModifyingSomeoneElsesWorld_ShouldReturn404() throws Exception {
        mockMvc.perform(put("/worlds/" + worldA.getWorldId()).contentType("application/json").content(objectMapper.writeValueAsString(updateWorld))).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "userA" , roles = "USER")
    void authenticated_User_CreatingWorld_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/worlds/create").contentType("application/json").content(objectMapper.writeValueAsString(createWorld))).andExpect(status().isOk()).andExpect(jsonPath("$.worldName").value("NewWorld"))
                .andExpect(jsonPath("$.worldDesc").value("NewDescription"));
    }

    @Test
    @WithMockUser(username = "userA" , roles = "USER")
    void authenticated_User_DeletingTheirWorld_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/worlds/" + worldC.getWorldId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "userB" , roles = "USER")
    void authenticated_User_DeletingSomeoneElsesWorld_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/worlds/" + worldA.getWorldId())).andExpect(status().isNotFound());
    }















}