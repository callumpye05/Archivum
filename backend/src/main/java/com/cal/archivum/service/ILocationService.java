package com.cal.archivum.service;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.dto.impl.CreateLocationDto;
import com.cal.archivum.dto.impl.LocationResponseDto;
import com.cal.archivum.dto.impl.UpdateLocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.World;

import java.util.List;


public interface ILocationService {

    List<LocationResponseDto> getAllLocationsByWorld(Long worldId);
    LocationResponseDto getLocation(Long id);
    LocationResponseDto createLocation(CreateLocationDto dto , Long worldId);
    LocationResponseDto updateLocation(Long id, UpdateLocationDto dto);
    void deleteLocation(Long id);
    Location transformFromDto(LocationDto dto , World world);
    LocationResponseDto getLocationByWorldId(Long worldId , Long locationId);
    LocationResponseDto toDto(Location location);
}
