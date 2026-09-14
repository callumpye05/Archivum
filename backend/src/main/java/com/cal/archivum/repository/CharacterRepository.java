package com.cal.archivum.repository;

import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<WorldCharacter, Long> {

    List<WorldCharacter> findAllByWorld(World world);
    Optional<WorldCharacter> findByCharacterIdAndWorldWorldIdAndWorldOwner(Long characterId, Long worldId , User user);
    Optional<WorldCharacter> findByCharacterIdAndWorldOwner(Long characterId , User user);

}
