package com.cal.archivum.service;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;

import java.util.List;


public interface ILocationService {

    List<Location> getAllLocationsByWorld(Long worldId);
    Location getLocation(Long id);
    Location createLocation(CreateLocationDto dto , Long worldId);
    Location updateLocation(Long id, UpdateLocationDto dto);
    void deleteLocation(Long id);
    Location transformFromDto(LocationDto dto , Long worldId);
}
