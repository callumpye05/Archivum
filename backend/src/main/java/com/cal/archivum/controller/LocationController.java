package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.LocationResponseDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.service.ILocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LocationController {

    private final ILocationService locationService;

    public LocationController(ILocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/worlds/{worldId}/locations")
    public List<LocationResponseDto> getAllLocationsFromWorld(@PathVariable long worldId) {
        return locationService.getAllLocationsByWorld(worldId);
    }

    @GetMapping("/locations/{locationId}")
    public LocationResponseDto getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }

    @GetMapping("/worlds/{worldId}/locations/{locationId}")
    public LocationResponseDto getLocationByWorld(@PathVariable Long locationId , @PathVariable Long worldId) {
        return locationService.getLocationByWorldId(worldId , locationId);
    }

    @PostMapping("/worlds/{worldId}/locations")
    public LocationResponseDto createLocation(@PathVariable Long worldId , @Valid @RequestBody CreateLocationDto dto ) {
        return locationService.createLocation(dto , worldId);
    }

    @PutMapping("/locations/{locationId}")
    public LocationResponseDto updateLocation(@PathVariable Long locationId , @Valid @RequestBody UpdateLocationDto dto) {
        return locationService.updateLocation(locationId , dto);
    }

    @DeleteMapping("/locations/{locationId}")
    public void deleteLocation(@PathVariable Long locationId) {
        locationService.deleteLocation(locationId);
    }
}
