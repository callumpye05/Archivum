package com.cal.archivum.service;

import com.cal.archivum.entity.World;

import java.util.List;
import java.util.Optional;

public interface IWorldService {
    List<World> getAllWorlds();
    World getWorld(Long id);
    World createWorld(World world);
    World updateWorld(Long id, World world);
    void deleteWorld(Long id);

}
