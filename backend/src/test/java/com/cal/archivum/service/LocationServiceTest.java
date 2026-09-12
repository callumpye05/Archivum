package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.enums.LocationType;
import com.cal.archivum.exception.LocationNotFound;
import com.cal.archivum.exception.LocationNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.impl.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WorldRepository worldRepository;

    @Mock
    private IUserService userService;

    private LocationService locationService;

    private User testUser;
    private World testWorld;
    private Location testLocation;

    @BeforeEach
    void setUp() {

        locationService =
                new LocationService(
                        locationRepository,
                        worldRepository,
                        userService
                );

        testUser = new User();
        testUser.setUserName("test_user");

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");
        testWorld.setOwner(testUser);

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
    // GET LOCATION
    // =========================================================

    @Test
    void getLocation_shouldReturnLocation_whenOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                10L,
                testUser
        )).thenReturn(Optional.of(testLocation));

        Location result =
                locationService.getLocation(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Ironspire", result.getLocationName());
        assertEquals(LocationType.CITY, result.getLocationType());

        verify(locationRepository)
                .findByIdAndWorldOwner(10L, testUser);
    }

    @Test
    void getLocation_shouldThrow_whenLocationNotAccessibleToCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.getLocation(99L)
        );

        verify(locationRepository)
                .findByIdAndWorldOwner(99L, testUser);
    }


    // =========================================================
    // GET ALL LOCATIONS FROM WORLD
    // =========================================================

    @Test
    void getAllLocationsByWorld_shouldReturnLocations_whenUserOwnsWorld() {

        Location secondLocation = new Location();
        secondLocation.setId(11L);
        secondLocation.setLocationName("Kronen Works");
        secondLocation.setLocationType(LocationType.FACILITY);
        secondLocation.setWorld(testWorld);

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepository.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(locationRepository.findAllByWorld(testWorld))
                .thenReturn(List.of(testLocation, secondLocation));

        List<Location> result =
                locationService.getAllLocationsByWorld(2L);

        assertEquals(2, result.size());
        assertEquals("Ironspire",
                result.get(0).getLocationName());

        assertEquals("Kronen Works",
                result.get(1).getLocationName());

        verify(worldRepository)
                .findByWorldIdAndOwner(2L, testUser);

        verify(locationRepository)
                .findAllByWorld(testWorld);
    }

    @Test
    void getAllLocationsByWorld_shouldReturnEmptyList_whenOwnedWorldHasNoLocations() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepository.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(locationRepository.findAllByWorld(testWorld))
                .thenReturn(List.of());

        List<Location> result =
                locationService.getAllLocationsByWorld(2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(locationRepository)
                .findAllByWorld(testWorld);
    }

    @Test
    void getAllLocationsByWorld_shouldThrow_whenWorldNotOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepository.findByWorldIdAndOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> locationService.getAllLocationsByWorld(99L)
        );

        verify(locationRepository, never())
                .findAllByWorld(any());
    }


    // =========================================================
    // GET LOCATION BY WORLD
    // =========================================================

    @Test
    void getLocationByWorldId_shouldReturnLocation_whenWorldAndOwnerMatch() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository
                .findByIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        2L,
                        testUser
                ))
                .thenReturn(Optional.of(testLocation));

        Location result =
                locationService.getLocationByWorldId(2L, 10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(2L, result.getWorld().getWorldId());

        verify(locationRepository)
                .findByIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        2L,
                        testUser
                );
    }

    @Test
    void getLocationByWorldId_shouldThrow_whenLocationNotAccessibleThroughWorld() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository
                .findByIdAndWorldWorldIdAndWorldOwner(
                        10L,
                        3L,
                        testUser
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundByWorld.class,
                () -> locationService
                        .getLocationByWorldId(3L, 10L)
        );
    }


    // =========================================================
    // CREATE LOCATION
    // =========================================================

    @Test
    void createLocation_shouldSaveLocation_whenUserOwnsWorld() {

        CreateLocationDto dto =
                new CreateLocationDto(
                        "Ironspire",
                        LocationType.CITY,
                        "A large industrial city."
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepository.findByWorldIdAndOwner(
                2L,
                testUser
        )).thenReturn(Optional.of(testWorld));

        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> {
                    Location location =
                            invocation.getArgument(0);
                    location.setId(10L);
                    return location;
                });

        Location result =
                locationService.createLocation(dto, 2L);

        assertNotNull(result);
        assertEquals("Ironspire",
                result.getLocationName());

        assertEquals(LocationType.CITY,
                result.getLocationType());

        assertEquals(
                "A large industrial city.",
                result.getLocationDescription()
        );

        assertSame(testWorld, result.getWorld());

        verify(worldRepository)
                .findByWorldIdAndOwner(2L, testUser);

        verify(locationRepository)
                .save(any(Location.class));
    }

    @Test
    void createLocation_shouldThrow_whenUserDoesNotOwnWorld() {

        CreateLocationDto dto =
                new CreateLocationDto(
                        "Ironspire",
                        LocationType.CITY,
                        "A large industrial city."
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(worldRepository.findByWorldIdAndOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> locationService.createLocation(dto, 99L)
        );

        verify(locationRepository, never())
                .save(any(Location.class));
    }


    // =========================================================
    // UPDATE LOCATION
    // =========================================================

    @Test
    void updateLocation_shouldUpdateProvidedFields_whenOwnedByUser() {

        UpdateLocationDto dto =
                new UpdateLocationDto(
                        "Ironspire Prime",
                        LocationType.DISTRICT,
                        "Updated location description."
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                10L,
                testUser
        )).thenReturn(Optional.of(testLocation));

        when(locationRepository.save(testLocation))
                .thenReturn(testLocation);

        Location result =
                locationService.updateLocation(10L, dto);

        assertEquals(
                "Ironspire Prime",
                result.getLocationName()
        );

        assertEquals(
                LocationType.DISTRICT,
                result.getLocationType()
        );

        assertEquals(
                "Updated location description.",
                result.getLocationDescription()
        );

        verify(locationRepository)
                .save(testLocation);
    }

    @Test
    void updateLocation_shouldOnlyUpdateNonNullFields() {

        UpdateLocationDto dto =
                new UpdateLocationDto(
                        null,
                        LocationType.LANDMARK,
                        null
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                10L,
                testUser
        )).thenReturn(Optional.of(testLocation));

        when(locationRepository.save(testLocation))
                .thenReturn(testLocation);

        Location result =
                locationService.updateLocation(10L, dto);

        assertEquals(
                "Ironspire",
                result.getLocationName()
        );

        assertEquals(
                LocationType.LANDMARK,
                result.getLocationType()
        );

        assertEquals(
                "A large industrial city.",
                result.getLocationDescription()
        );
    }

    @Test
    void updateLocation_shouldThrow_whenLocationNotOwnedByCurrentUser() {

        UpdateLocationDto dto =
                new UpdateLocationDto(
                        "Updated",
                        LocationType.CITY,
                        "Updated"
                );

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.updateLocation(99L, dto)
        );

        verify(locationRepository, never())
                .save(any());
    }


    // =========================================================
    // DELETE LOCATION
    // =========================================================

    @Test
    void deleteLocation_shouldDelete_whenLocationOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                10L,
                testUser
        )).thenReturn(Optional.of(testLocation));

        locationService.deleteLocation(10L);

        verify(locationRepository)
                .findByIdAndWorldOwner(10L, testUser);

        verify(locationRepository)
                .delete(testLocation);
    }

    @Test
    void deleteLocation_shouldThrow_whenLocationNotOwnedByCurrentUser() {

        when(userService.getCurrentUser())
                .thenReturn(testUser);

        when(locationRepository.findByIdAndWorldOwner(
                99L,
                testUser
        )).thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.deleteLocation(99L)
        );

        verify(locationRepository, never())
                .delete(any(Location.class));
    }


    // =========================================================
    // TRANSFORM DTO
    // =========================================================

    @Test
    void transformFromDto_shouldCreateLocationWithCorrectWorld() {

        CreateLocationDto dto =
                new CreateLocationDto(
                        "Ironspire",
                        LocationType.CITY,
                        "A large industrial city."
                );

        Location result =
                locationService.transformFromDto(dto, testWorld);

        assertEquals(
                "Ironspire",
                result.getLocationName()
        );

        assertEquals(
                LocationType.CITY,
                result.getLocationType()
        );

        assertEquals(
                "A large industrial city.",
                result.getLocationDescription()
        );

        assertSame(testWorld, result.getWorld());
    }
}