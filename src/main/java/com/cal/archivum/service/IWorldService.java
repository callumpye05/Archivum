package com.cal.archivum.service;

import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;

import java.util.List;

public interface IWorldService {
    List<World> getAllWorlds();
    World getWorld(Long id);
    World createWorld(CreateWorldDto dto);
    World updateWorld(Long id, UpdateWorldDto dto);
    void deleteWorld(Long id); //TODO : Cascade deletion needed on characters
    World transformFromDto(WorldDto dto);

}
