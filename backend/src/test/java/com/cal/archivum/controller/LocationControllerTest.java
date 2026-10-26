package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.World;
import com.cal.archivum.enums.LocationType;
import com.cal.archivum.exception.LocationNotFound;
import com.cal.archivum.exception.LocationNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.security.ArchivumSecurityConfig;
import com.cal.archivum.service.ILocationService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@Import(ArchivumSecurityConfig.class)
@WithMockUser(username = "test_user",roles = "USER")
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ILocationService locationService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private World testWorld;
    private Location testLocation;


    @BeforeEach
    void setUp() {

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");
        testWorld.setWorldDesc("Industrial world");

        testLocation = new Location();
        testLocation.setId(10L);
        testLocation.setLocationName("Ironspire");
        testLocation.setLocationType(LocationType.CITY);
        testLocation.setLocationDescription(
                "A large industrial city."
        );
        testLocation.setWorld(testWorld);
    }


    // =========================================================
    // SECURITY
    // =========================================================

    @Test
    @WithAnonymousUser
    void getLocation_shouldReturn401_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/locations/10")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(locationService);
    }


    // =========================================================
    // GET LOCATION
    // =========================================================

    @Test
    void getLocation_shouldReturn200_whenLocationExists()
            throws Exception {

        when(locationService.getLocation(10L))
                .thenReturn(testLocation);

        mockMvc.perform(
                        get("/locations/10")
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.id")
                                .value(10L)
                )

                .andExpect(
                        jsonPath("$.locationName")
                                .value("Ironspire")
                )

                .andExpect(
                        jsonPath("$.locationType")
                                .value("CITY")
                )

                .andExpect(
                        jsonPath("$.locationDescription")
                                .value(
                                        "A large industrial city."
                                )
                );

        verify(locationService)
                .getLocation(10L);
    }


    @Test
    void getLocation_shouldReturn404_whenLocationDoesNotExist()
            throws Exception {

        when(locationService.getLocation(99L))
                .thenThrow(
                        new LocationNotFound(99L)
                );

        mockMvc.perform(
                        get("/locations/99")
                )
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/locations/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());

        verify(locationService)
                .getLocation(99L);
    }


    // =========================================================
    // GET ALL LOCATIONS FROM WORLD
    // =========================================================

    @Test
    void getAllLocationsFromWorld_shouldReturn200_whenWorldExists()
            throws Exception {

        when(
                locationService
                        .getAllLocationsByWorld(2L)
        )
                .thenReturn(
                        List.of(testLocation)
                );

        mockMvc.perform(
                        get("/worlds/2/locations")
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$[0].id")
                                .value(10L)
                )

                .andExpect(
                        jsonPath("$[0].locationName")
                                .value("Ironspire")
                )

                .andExpect(
                        jsonPath("$[0].locationType")
                                .value("CITY")
                )

                .andExpect(
                        jsonPath("$[0].locationDescription")
                                .value(
                                        "A large industrial city."
                                )
                );

        verify(locationService)
                .getAllLocationsByWorld(2L);
    }


    @Test
    void getAllLocationsFromWorld_shouldReturn200AndEmptyList_whenWorldHasNoLocations()
            throws Exception {

        when(
                locationService
                        .getAllLocationsByWorld(2L)
        )
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/worlds/2/locations")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );

        verify(locationService)
                .getAllLocationsByWorld(2L);
    }


    @Test
    void getAllLocationsFromWorld_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        when(
                locationService
                        .getAllLocationsByWorld(99L)
        )
                .thenThrow(
                        new WorldNotFound(99L)
                );

        mockMvc.perform(
                        get("/worlds/99/locations")
                )
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/99/locations")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());

        verify(locationService)
                .getAllLocationsByWorld(99L);
    }


    // =========================================================
    // GET LOCATION FROM SPECIFIC WORLD
    // =========================================================

    @Test
    void getLocationByWorld_shouldReturn200_whenLocationBelongsToWorld()
            throws Exception {

        when(
                locationService
                        .getLocationByWorldId(
                                2L,
                                10L
                        )
        )
                .thenReturn(testLocation);

        mockMvc.perform(
                        get("/worlds/2/locations/10")
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.id")
                                .value(10L)
                )

                .andExpect(
                        jsonPath("$.locationName")
                                .value("Ironspire")
                )

                .andExpect(
                        jsonPath("$.locationType")
                                .value("CITY")
                );

        verify(locationService)
                .getLocationByWorldId(
                        2L,
                        10L
                );
    }


    @Test
    void getLocationByWorld_shouldReturn404_whenLocationDoesNotBelongToWorld()
            throws Exception {

        when(
                locationService
                        .getLocationByWorldId(
                                3L,
                                10L
                        )
        )
                .thenThrow(
                        new LocationNotFoundByWorld(
                                10L,
                                3L
                        )
                );

        mockMvc.perform(
                        get("/worlds/3/locations/10")
                )
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/3/locations/10")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());

        verify(locationService)
                .getLocationByWorldId(
                        3L,
                        10L
                );
    }


    // =========================================================
    // CREATE LOCATION
    // =========================================================

    @Test
    void createLocation_shouldReturn200_whenRequestIsValid()
            throws Exception {

        CreateLocationDto dto =
                new CreateLocationDto("Ironshield", LocationType.CITY, "Industrial city");
        when(locationService.createLocation(any(CreateLocationDto.class), eq(2L))).thenReturn(testLocation);

        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10L)
                )
                .andExpect(
                        jsonPath("$.locationName")
                                .value("Ironspire")
                )
                .andExpect(
                        jsonPath("$.locationType")
                                .value("CITY")
                );

        verify(locationService)
                .createLocation(
                        any(CreateLocationDto.class),
                        eq(2L)
                );
    }


    @Test
    void createLocation_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("   ", LocationType.CITY, "Industrial city");
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage").value("Location name cannot be null or empty")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn400_whenTypeIsNull() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("Ironshield", null, "Industrial city");
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage").value("Location type cannot be null")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn400_whenDescriptionIsNull() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("Ironshield", LocationType.CITY, null);
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage").value("Location description cannot be null")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn400_whenNameExceeds100Characters() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("A".repeat(101), LocationType.CITY, "Industrial city");
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage").value("Max location size is 100")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn400_whenDescriptionExceeds500Characters() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("Ironshield", LocationType.CITY, "A".repeat(501));
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage").value("Max size for the location description is 500")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn400_whenMultipleFieldsAreInvalid() throws Exception {
        CreateLocationDto dto=new CreateLocationDto("", null, "Industrial city");
        mockMvc.perform(post("/worlds/2/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/2/locations")).andExpect(jsonPath("$.errorMessage", containsString("Location name cannot be null or empty"))).andExpect(jsonPath("$.errorMessage", containsString("Location type cannot be null"))).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void createLocation_shouldReturn404_whenWorldDoesNotExist()
            throws Exception {

        CreateLocationDto dto = new CreateLocationDto("Ironshield", LocationType.CITY, "A large city."
        );

        when(
                locationService.createLocation(
                        any(CreateLocationDto.class),
                        eq(99L)
                )
        )
                .thenThrow(
                        new WorldNotFound(99L)
                );

        mockMvc.perform(post("/worlds/99/locations").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/worlds/99/locations")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());

        verify(locationService)
                .createLocation(
                        any(CreateLocationDto.class),
                        eq(99L)
                );
    }


    // =========================================================
    // UPDATE LOCATION
    // =========================================================

    @Test
    void updateLocation_shouldReturn200_whenRequestIsValid()
            throws Exception {

        UpdateLocationDto dto = new UpdateLocationDto("Ironshield Prime", LocationType.DISTRICT, "Updated description");

        Location updatedLocation = new Location();

        updatedLocation.setId(10L);
        updatedLocation.setLocationName(
                "Ironshield Prime"
        );
        updatedLocation.setLocationType(
                LocationType.DISTRICT
        );
        updatedLocation.setLocationDescription(
                "Updated description"
        );
        updatedLocation.setWorld(testWorld);

        when(locationService.updateLocation(eq(10L), any(UpdateLocationDto.class))).thenReturn(updatedLocation);

        mockMvc.perform(put("/locations/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(10L)
                )
                .andExpect(
                        jsonPath("$.locationName")
                                .value("Ironshield Prime")
                )
                .andExpect(
                        jsonPath("$.locationType")
                                .value("DISTRICT")
                )

                .andExpect(
                        jsonPath("$.locationDescription")
                                .value(
                                        "Updated description"
                                )
                );

        verify(locationService)
                .updateLocation(
                        eq(10L),
                        any(UpdateLocationDto.class)
                );
    }


    @Test
    void updateLocation_shouldReturn400_whenNameIsBlank() throws Exception {
        UpdateLocationDto dto=new UpdateLocationDto("   ", null, null);
        mockMvc.perform(put("/locations/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/locations/10")).andExpect(jsonPath("$.errorMessage").value("Location name cannot be blank")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void updateLocation_shouldReturn400_whenNameExceeds100Characters() throws Exception {
        UpdateLocationDto dto=new UpdateLocationDto("A".repeat(101), null, null);
        mockMvc.perform(put("/locations/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/locations/10")).andExpect(jsonPath("$.errorMessage").value("Location name must not exceed 100 characters")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void updateLocation_shouldReturn400_whenDescriptionExceeds500Characters() throws Exception {
        UpdateLocationDto dto=new UpdateLocationDto(null, null, "A".repeat(501));
        mockMvc.perform(put("/locations/10").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(400)).andExpect(jsonPath("$.statusText").value("Bad Request")).andExpect(jsonPath("$.apiPath").value("uri=/locations/10")).andExpect(jsonPath("$.errorMessage").value("Location description must not exceed 500 characters")).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verifyNoInteractions(locationService);
    }

    @Test
    void updateLocation_shouldReturn404_whenLocationDoesNotExist() throws Exception {
        UpdateLocationDto dto = new UpdateLocationDto("Updated", LocationType.CITY, "Updated description");
        when(locationService.updateLocation(eq(99L), any(UpdateLocationDto.class))).thenThrow(new LocationNotFound(99L));
        mockMvc.perform(put("/locations/99").with(csrf()).contentType("application/json").content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/locations/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());

        verify(locationService).updateLocation(eq(99L), any(UpdateLocationDto.class));
    }


    // =========================================================
    // DELETE LOCATION
    // =========================================================

    @Test
    void deleteLocation_shouldReturn200_whenLocationExists() throws Exception {
        doNothing().when(locationService).deleteLocation(10L);
        mockMvc.perform(delete("/locations/10").with(csrf())).andExpect(status().isOk());
        verify(locationService).deleteLocation(10L);
    }

    @Test
    void deleteLocation_shouldReturn404_whenLocationDoesNotExist() throws Exception {
        doThrow(new LocationNotFound(99L)).when(locationService).deleteLocation(99L);
        mockMvc.perform(delete("/locations/99").with(csrf())).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode").value(404)).andExpect(jsonPath("$.statusText").value("Not Found")).andExpect(jsonPath("$.apiPath").value("uri=/locations/99")).andExpect(jsonPath("$.errorMessage").isNotEmpty()).andExpect(jsonPath("$.errorTime").isNotEmpty());
        verify(locationService).deleteLocation(99L);
    }


}
