package com.cal.archivum.service;



import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
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

    private LocationService locationService;

    private World testWorld;
    private Location testLocation;

    @BeforeEach
    void setUp() {

        locationService =
                new LocationService(locationRepository, worldRepository);

        testWorld = new World();
        testWorld.setWorldId(2L);
        testWorld.setWorldName("Eisenmark");

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
    void getLocation_shouldReturnLocation_whenLocationExists() {

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(testLocation));

        Location result = locationService.getLocation(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Ironspire", result.getLocationName());
        assertEquals(LocationType.CITY, result.getLocationType());

        verify(locationRepository).findById(10L);
    }

    @Test
    void getLocation_shouldThrowLocationNotFound_whenLocationDoesNotExist() {

        when(locationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.getLocation(99L)
        );

        verify(locationRepository).findById(99L);
    }


    // =========================================================
    // GET ALL LOCATIONS FROM WORLD
    // =========================================================

    @Test
    void getAllLocationsByWorld_shouldReturnLocations_whenWorldExists() {

        Location secondLocation = new Location();
        secondLocation.setId(11L);
        secondLocation.setLocationName("Kronen Works");
        secondLocation.setLocationType(LocationType.FACILITY);
        secondLocation.setWorld(testWorld);

        when(worldRepository.findById(2L))
                .thenReturn(Optional.of(testWorld));

        when(locationRepository.findAllByWorld(testWorld))
                .thenReturn(List.of(testLocation, secondLocation));

        List<Location> result =
                locationService.getAllLocationsByWorld(2L);

        assertEquals(2, result.size());
        assertEquals("Ironspire", result.get(0).getLocationName());
        assertEquals("Kronen Works", result.get(1).getLocationName());

        verify(worldRepository).findById(2L);
        verify(locationRepository).findAllByWorld(testWorld);
    }

    @Test
    void getAllLocationsByWorld_shouldReturnEmptyList_whenWorldExistsButHasNoLocations() {

        when(worldRepository.findById(2L))
                .thenReturn(Optional.of(testWorld));

        when(locationRepository.findAllByWorld(testWorld))
                .thenReturn(List.of());

        List<Location> result =
                locationService.getAllLocationsByWorld(2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(locationRepository).findAllByWorld(testWorld);
    }

    @Test
    void getAllLocationsByWorld_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        when(worldRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WorldNotFound.class,
                () -> locationService.getAllLocationsByWorld(99L)
        );

        verify(worldRepository).findById(99L);

        verify(locationRepository, never())
                .findAllByWorld(any());
    }


    // =========================================================
    // GET LOCATION BY WORLD
    // =========================================================

    @Test
    void getLocationByWorldId_shouldReturnLocation_whenLocationBelongsToWorld() {

        when(locationRepository.findByIdAndWorldWorldId(10L, 2L))
                .thenReturn(Optional.of(testLocation));

        Location result =
                locationService.getLocationByWorldId(2L, 10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(2L, result.getWorld().getWorldId());

        verify(locationRepository)
                .findByIdAndWorldWorldId(10L, 2L);
    }

    @Test
    void getLocationByWorldId_shouldThrow_whenLocationDoesNotBelongToWorld() {

        when(locationRepository.findByIdAndWorldWorldId(10L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(
                LocationNotFoundByWorld.class,
                () -> locationService.getLocationByWorldId(3L, 10L)
        );

        verify(locationRepository)
                .findByIdAndWorldWorldId(10L, 3L);
    }


    // =========================================================
    // CREATE LOCATION
    // =========================================================

    @Test
    void createLocation_shouldSaveLocation_whenWorldExists() {

        CreateLocationDto dto = new CreateLocationDto(
                "Ironspire",
                LocationType.CITY,
                "A large industrial city."
        );

        when(worldRepository.findById(2L))
                .thenReturn(Optional.of(testWorld));

        when(locationRepository.save(any(Location.class)))
                .thenAnswer(invocation -> {
                    Location location = invocation.getArgument(0);
                    location.setId(10L);
                    return location;
                });

        Location result =
                locationService.createLocation(dto, 2L);

        assertNotNull(result);
        assertEquals("Ironspire", result.getLocationName());
        assertEquals(LocationType.CITY, result.getLocationType());
        assertEquals("A large industrial city.",
                result.getLocationDescription());

        assertEquals(testWorld, result.getWorld());

        verify(worldRepository).findById(2L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_shouldThrowWorldNotFound_whenWorldDoesNotExist() {

        CreateLocationDto dto = new CreateLocationDto(
                "Ironspire",
                LocationType.CITY,
                "A large industrial city."
        );

        when(worldRepository.findById(99L))
                .thenReturn(Optional.empty());

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
    void updateLocation_shouldUpdateProvidedFields() {

        UpdateLocationDto dto = new UpdateLocationDto(
                "Ironspire Prime",
                LocationType.DISTRICT,
                "Updated location description."
        );

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(testLocation));

        when(locationRepository.save(testLocation))
                .thenReturn(testLocation);

        Location result =
                locationService.updateLocation(10L, dto);

        assertEquals("Ironspire Prime", result.getLocationName());
        assertEquals(
                LocationType.DISTRICT,
                result.getLocationType()
        );
        assertEquals(
                "Updated location description.",
                result.getLocationDescription()
        );

        verify(locationRepository).save(testLocation);
    }

    @Test
    void updateLocation_shouldOnlyUpdateNonNullFields() {

        UpdateLocationDto dto = new UpdateLocationDto(
                null,
                LocationType.LANDMARK,
                null
        );

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(testLocation));

        when(locationRepository.save(testLocation))
                .thenReturn(testLocation);

        Location result =
                locationService.updateLocation(10L, dto);

        assertEquals("Ironspire", result.getLocationName());
        assertEquals(LocationType.LANDMARK, result.getLocationType());
        assertEquals(
                "A large industrial city.",
                result.getLocationDescription()
        );

        verify(locationRepository).save(testLocation);
    }

    @Test
    void updateLocation_shouldThrowLocationNotFound_whenLocationDoesNotExist() {

        UpdateLocationDto dto = new UpdateLocationDto(
                "Updated",
                LocationType.CITY,
                "Updated"
        );

        when(locationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.updateLocation(99L, dto)
        );

        verify(locationRepository, never()).save(any());
    }


    // =========================================================
    // DELETE LOCATION
    // =========================================================

    @Test
    void deleteLocation_shouldDeleteLocation_whenLocationExists() {

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(testLocation));

        locationService.deleteLocation(10L);

        verify(locationRepository).findById(10L);
        verify(locationRepository).deleteById(10L);
    }

    @Test
    void deleteLocation_shouldThrowLocationNotFound_whenLocationDoesNotExist() {

        when(locationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LocationNotFound.class,
                () -> locationService.deleteLocation(99L)
        );

        verify(locationRepository, never())
                .deleteById(anyLong());
    }


    // =========================================================
    // TRANSFORM DTO
    // =========================================================

    @Test
    void transformFromDto_shouldCreateLocationWithCorrectWorld() {

        CreateLocationDto dto = new CreateLocationDto(
                "Ironspire",
                LocationType.CITY,
                "A large industrial city."
        );

        when(worldRepository.findById(2L))
                .thenReturn(Optional.of(testWorld));

        Location result =
                locationService.transformFromDto(dto, 2L);

        assertEquals("Ironspire", result.getLocationName());
        assertEquals(LocationType.CITY, result.getLocationType());
        assertEquals(
                "A large industrial city.",
                result.getLocationDescription()
        );
        assertEquals(testWorld, result.getWorld());
    }
}
