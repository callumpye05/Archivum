package com.cal.archivum.service.impl;

import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.IWorldService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorldService implements IWorldService {

    private final WorldRepository worldRepo;

    public WorldService(WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
    }

    @Override
    public World createWorld(CreateWorldDto dto) {
        return worldRepo.save(transformFromDto(dto)); //Already saves the value in the method
    }

    @Override
    public World updateWorld(Long id, UpdateWorldDto dto) {
        World  existingWorld = worldRepo.findById(id).orElseThrow(() -> new WorldNotFound(id));


        if(dto.worldDesc() != null) {
            existingWorld.setWorldDesc(dto.worldDesc());
        }
        if(dto.worldName() != null) {
            existingWorld.setWorldName(dto.worldName());
        }

        return worldRepo.save(existingWorld);
    }

    @Override
    public void deleteWorld(Long id) {
        worldRepo.findById(id).orElseThrow(() -> new WorldNotFound(id));
        worldRepo.deleteById(id);
    }

    @Override
    public List<World> getAllWorlds() {
        return worldRepo.findAll();
    }

    @Override
    public World getWorld(Long id) {
        return worldRepo.findById(id).orElseThrow(() -> new WorldNotFound(id));
    }

    @Override
    public World transformFromDto(WorldDto dto) {
        World world = new World();
        world.setWorldDesc(dto.worldDesc());
        world.setWorldName(dto.worldName());

        return world;
    }
}
