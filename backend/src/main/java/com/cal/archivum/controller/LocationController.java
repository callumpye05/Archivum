package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateLocationDto;
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
    public List<Location> getAllLocationsFromWorld(@PathVariable long worldId) {
        return locationService.getAllLocationsByWorld(worldId);
    }

    @GetMapping("/locations/{locationId}")
    public Location getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }

    @GetMapping("/worlds/{worldId}/locations/{locationId}")
    public Location getLocationByWorld(@PathVariable Long locationId , @PathVariable Long worldId) {
        return locationService.getLocationByWorldId(worldId , locationId);
    }

    @PostMapping("/worlds/{worldId}/locations")
    public Location createLocation(@PathVariable Long worldId , @Valid @RequestBody CreateLocationDto dto ) {
        return locationService.createLocation(dto , worldId);
    }

    @PutMapping("/locations/{locationId}")
    public Location updateLocation(@PathVariable Long locationId , @Valid @RequestBody UpdateLocationDto dto) {
        return locationService.updateLocation(locationId , dto);
    }

    @DeleteMapping("/locations/{locationId}")
    public void deleteLocation(@PathVariable Long locationId) {
        locationService.deleteLocation(locationId);
    }
}
