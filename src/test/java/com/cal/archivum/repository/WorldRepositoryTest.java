package com.cal.archivum.repository;

import com.cal.archivum.entity.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop"})
public class WorldRepositoryTest {

    @Autowired
    private WorldRepository worldRepo;

    private World testWorld;
    private World testWorld2;


    @BeforeEach
    void setup() {
        testWorld = new World();
        testWorld.setWorldName("Test World");
        testWorld.setWorldDesc("World used for repository testing");
        testWorld = worldRepo.save(testWorld);
        testWorld2 = new World();
        testWorld2.setWorldName("Test World2");
        testWorld2.setWorldDesc("World used for repository testing");
        testWorld2 = worldRepo.save(testWorld2);
    }

    @Test
    void findById_shouldReturnWorld_whenWorldExists() {

        Optional<World> result =
                worldRepo.findById(testWorld.getWorldId());
        assertTrue(result.isPresent());
        assertEquals("Test World", result.get().getWorldName());
    }

    @Test
    void getAllWorlds_shouldReturnAllWorlds() {

        List<World> result =
                worldRepo.findAll();

        assertEquals(2, result.size());
    }





}
