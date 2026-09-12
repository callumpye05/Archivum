package com.cal.archivum.service.impl;


import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.LocationNotFound;
import com.cal.archivum.exception.LocationNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.ILocationService;
import com.cal.archivum.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService implements ILocationService {

    private final LocationRepository locationRepository;
    private final WorldRepository worldRepository;
    private final IUserService userService;

    public LocationService(LocationRepository locationRepository, WorldRepository worldRepository, IUserService userService) {
        this.locationRepository = locationRepository;
        this.worldRepository = worldRepository;
        this.userService = userService;
    }

    @Override
    public List<Location> getAllLocationsByWorld(Long worldId) {
        User currentUser = userService.getCurrentUser();
        World world = worldRepository.findByWorldIdAndOwner(worldId, currentUser).orElseThrow(() -> new WorldNotFound(worldId));
        return locationRepository.findAllByWorld(world);
    }

    @Override
    public Location getLocation(Long id) {
        User currentUser = userService.getCurrentUser();
        return locationRepository.findByIdAndWorldOwner(id , currentUser).orElseThrow(() -> new LocationNotFound(id));
    }

    @Override
    public Location getLocationByWorldId(Long worldId, Long locationId) {
        User currentUser = userService.getCurrentUser();
        return locationRepository.findByIdAndWorldWorldIdAndWorldOwner(locationId , worldId , currentUser).orElseThrow(()-> new LocationNotFoundByWorld(locationId, worldId));
    }

    @Override
    public Location createLocation(CreateLocationDto dto, Long worldId) {
        User currentUser = userService.getCurrentUser();
        World world = worldRepository.findByWorldIdAndOwner(worldId , currentUser).orElseThrow(() -> new WorldNotFound(worldId));
        Location newLocation = transformFromDto(dto , world);
        return locationRepository.save(newLocation);
    }

    @Override
    public Location updateLocation(Long id, UpdateLocationDto dto) {
        User currentUser = userService.getCurrentUser();
        Location existingLocation= locationRepository.findByIdAndWorldOwner(id, currentUser).orElseThrow(() -> new LocationNotFound(id));

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
        User currentUser = userService.getCurrentUser();
        Location existingLocation = locationRepository.findByIdAndWorldOwner(id, currentUser).orElseThrow(() -> new LocationNotFound(id));
        locationRepository.delete(existingLocation);
    }

    @Override
    public Location transformFromDto(LocationDto dto, World world) {

        Location location = new Location();
        location.setLocationName(dto.locationName());
        location.setLocationType(dto.locationType());
        location.setLocationDescription(dto.locationDesc());
        location.setWorld(world);
        return location;
    }
}

