package com.cal.archivum.service;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.entity.Location;
import com.cal.archivum.entity.WorldCharacter;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ILocationService {

    List<Location> getAllLocationsByWorld(Long worldId);
    Location getLocation(Long id);
    Location createLocation(LocationDto dto , Long worldId);
    Location updateLocation(Long id, LocationDto dto);
    void deleteLocation(Long id);
    Location transformFromDto(LocationDto dto , Long worldId);
}
