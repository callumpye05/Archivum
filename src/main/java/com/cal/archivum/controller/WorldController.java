package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateWorldDto;
import com.cal.archivum.dto.impl.UpdateWorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.service.IWorldService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WorldController {

    private final IWorldService worldService;

    public WorldController(IWorldService worldService) {
        this.worldService = worldService;
    }

    @GetMapping("/worlds")
    public List<World> getAllWorlds() {
        return worldService.getAllWorlds();
    }

    @GetMapping("/worlds/search/{id}")
    public World getWorld(@PathVariable Long id) {
       return worldService.getWorld(id);
    }

    @PostMapping("worlds/create")
    public World createWorld( @Valid @RequestBody CreateWorldDto dto) {
        return worldService.createWorld(dto);

    }

    @PutMapping("worlds/update/{id}")
    public World updateWorld(@PathVariable Long id, @Valid @RequestBody UpdateWorldDto dto) {
        return worldService.updateWorld(id , dto);

    }

    @DeleteMapping("worlds/delete/{id}")
    public void deleteWorld(@PathVariable Long id) {

        worldService.deleteWorld(id);
    }
}
