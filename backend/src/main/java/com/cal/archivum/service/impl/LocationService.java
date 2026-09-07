package com.cal.archivum.service.impl;


import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.LocationNotFound;
import com.cal.archivum.exception.LocationNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.ILocationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService implements ILocationService {

    private final LocationRepository locationRepository;
    private final WorldRepository worldRepository;

    public LocationService(LocationRepository locationRepository, WorldRepository worldRepository) {
        this.locationRepository = locationRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public List<Location> getAllLocationsByWorld(Long worldId) {
        World world = worldRepository.findById(worldId).orElseThrow(() -> new WorldNotFound(worldId));
        return locationRepository.findAllByWorld(world);
    }

    @Override
    public Location getLocation(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new LocationNotFound(id));
    }

    @Override
    public Location getLocationByWorldId(Long worldId, Long locationId) {
        return locationRepository.findByIdAndWorldWorldId(locationId , worldId).orElseThrow(()-> new LocationNotFoundByWorld(locationId, worldId));
    }

    @Override
    public Location createLocation(CreateLocationDto dto, Long worldId) {
        Location newLocation = transformFromDto(dto , worldId);
        return locationRepository.save(newLocation);
    }

    @Override
    public Location updateLocation(Long id, UpdateLocationDto dto) {
        Location existingLocation= locationRepository.findById(id).orElseThrow(() -> new LocationNotFound(id));

        if(dto.locationName() != null) {
            existingLocation.setLocationName(dto.locationName());
        }
        if(dto.locationDesc() != null) {
            existingLocation.setLocationDescription(dto.locationDesc());
        }
        if(dto.locationType() != null) {
           existingLocation.setLocationType(dto.locationType());
        }

        return locationRepository.save(existingLocation);
    }


    @Override
    public void deleteLocation(Long id) {
        locationRepository.findById(id).orElseThrow(() -> new LocationNotFound(id));
        locationRepository.deleteById(id);
    }

    @Override
    public Location transformFromDto(LocationDto dto, Long worldId) {

        Location location = new Location();
        World world =worldRepository.findById(worldId).orElseThrow(() -> new WorldNotFound(worldId));
        location.setLocationName(dto.locationName());
        location.setLocationType(dto.locationType());
        location.setLocationDescription(dto.locationDesc());
        location.setWorld(world);
        return location;
    }
}

