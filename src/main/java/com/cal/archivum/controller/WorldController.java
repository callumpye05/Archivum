package com.cal.archivum.controller;

import com.cal.archivum.entity.World;
import com.cal.archivum.service.IWorldService;
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
    public World createWorld(@RequestBody World world) {
        return worldService.createWorld(world);

    }

    @PutMapping("worlds/update/{id}")
    public void updateWorld(@PathVariable Long id, @RequestBody World world ) {


    }

    @DeleteMapping("worlds/delete/{id}")
    public void deleteWorld(@PathVariable Long id) {
        //TODO: Call service as well
    }
}
