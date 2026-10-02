package com.cal.archivum.service.impl;

import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.IUserService;
import com.cal.archivum.service.IWorldService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorldService implements IWorldService {

    private final WorldRepository worldRepo;
    private final IUserService userService;
    private final CharacterRepository characterRepo;
    private final LocationRepository locationRepo;


    public WorldService(WorldRepository worldRepo, IUserService userService, CharacterRepository characterRepo, LocationRepository locationRepo) {
        this.worldRepo = worldRepo;
        this.userService = userService;
        this.characterRepo = characterRepo;
        this.locationRepo = locationRepo;
    }

    @Override
    public World createWorld(CreateWorldDto dto) {
        World world = transformFromDto(dto);
        world.setOwner(userService.getCurrentUser());

        return worldRepo.save(world);
    }

    @Override
    public World updateWorld(Long id, UpdateWorldDto dto) {
        User currentUser = userService.getCurrentUser();

        World  existingWorld = worldRepo.findByWorldIdAndOwner(id, currentUser).orElseThrow(() -> new WorldNotFound(id));


        if(dto.worldDesc() != null) {
            existingWorld.setWorldDesc(dto.worldDesc());
        }
        if(dto.worldName() != null) {
            existingWorld.setWorldName(dto.worldName());
        }
        return worldRepo.save(existingWorld);
    }
    @Transactional
    @Override
    public void deleteWorld(Long id) {

        User currentUser = userService.getCurrentUser();
        World existingWorld = worldRepo.findByWorldIdAndOwner(id , currentUser).orElseThrow(() -> new WorldNotFound(id));

        characterRepo.deleteAllByWorld(existingWorld);
        locationRepo.deleteAllByWorld(existingWorld);
        worldRepo.delete(existingWorld);
    }




    @Override
    public List<World> getAllWorlds() {
        User currentUser = userService.getCurrentUser();
        return worldRepo.findAllByOwner(currentUser);
    }

    @Override
    public World getWorld(Long id) {

        User currentUser = userService.getCurrentUser();
        return worldRepo.findByWorldIdAndOwner(id, currentUser).orElseThrow(() -> new WorldNotFound(id));
    }






    @Override
    public World transformFromDto(WorldDto dto) {
        World world = new World();
        world.setWorldDesc(dto.worldDesc());
        world.setWorldName(dto.worldName());
        return world;
    }
}
