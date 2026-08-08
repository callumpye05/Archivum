package com.cal.archivum.service;

import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.entity.World;

import java.util.List;
import java.util.Optional;

public interface IWorldService {
    List<World> getAllWorlds();
    World getWorld(Long id);
    World createWorld(WorldDto dto);
    World updateWorld(Long id, WorldDto dto);
    void deleteWorld(Long id);
    World transformFromDto(WorldDto dto);

}
