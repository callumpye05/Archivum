package com.cal.archivum.repository;

import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterRepository extends JpaRepository<WorldCharacter, Long> {
    List<WorldCharacter> findAllByWorld(World world);
}
