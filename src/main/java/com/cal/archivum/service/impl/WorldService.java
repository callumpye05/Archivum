package com.cal.archivum.service.impl;

import com.cal.archivum.entity.World;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.IWorldService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorldService implements IWorldService {

    private final WorldRepository worldRepo;

    public WorldService(WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
    }

    @Override
    public World createWorld(World world) {
        return worldRepo.save(world);
    }

    @Override
    public World updateWorld(Long id, World world) {
        World  existingWorld = worldRepo.getById(id);

        //then check what is null and what isn't
        if(world.getWorldDesc() != null) {
            existingWorld.setWorldDesc(world.getWorldDesc());
        }
        if(world.getWorldName() != null) {
            existingWorld.setWorldName(world.getWorldName());
        }

        return worldRepo.save(existingWorld);
    }

    @Override
    public void deleteWorld(Long id) {
        worldRepo.deleteById(id);
    }

    @Override
    public List<World> getAllWorlds() {
        return worldRepo.findAll();
    }

    @Override
    public World getWorld(Long id) {
        return worldRepo.findById(id).orElseThrow();
    }
}
