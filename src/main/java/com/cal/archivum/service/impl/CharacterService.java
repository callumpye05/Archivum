package com.cal.archivum.service.impl;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.exception.CharacterNotFound;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.ICharacterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharacterService implements ICharacterService {

    private final CharacterRepository characterRepo;
    private final WorldRepository worldRepo;

    public CharacterService(CharacterRepository characterRepo, WorldRepository worldRepo) {
        this.characterRepo = characterRepo;
        this.worldRepo = worldRepo;
    }

    @Override
    public List<WorldCharacter> getAllCharactersFromWorld(Long worldId) {
        World world = worldRepo.findById(worldId).orElseThrow(() -> new WorldNotFound(worldId));
        return characterRepo.findAllByWorld(world);
    }

    @Override
    public WorldCharacter getCharacter(Long id) {
       return characterRepo.findById(id).orElseThrow(() -> new CharacterNotFound(id));
    }

    @Override
    public WorldCharacter createCharacter(CharacterDto dto , Long worldId) {
        worldRepo.findById(worldId).orElseThrow(()-> new WorldNotFound(worldId));
        WorldCharacter character = transformFromDto(dto,worldId);
        return characterRepo.save(character);
    }

    @Override
    public WorldCharacter updateCharacter(Long id, CharacterDto dto) {
        WorldCharacter existingCharacter = characterRepo.findById(id).orElseThrow(()-> new CharacterNotFound(id));

        if(dto.characterName() != null) {
            existingCharacter.setCharacterName(dto.characterName());
        }
        if(dto.characterSpecies() != null) {
            existingCharacter.setCharacterSpecies(dto.characterSpecies());
        }
        if(dto.age() != null) {
            existingCharacter.setAge(dto.age());
        }
        if(dto.characterDescription() != null) {
            existingCharacter.setCharacterDescription(dto.characterDescription());
        }
        if(dto.characterNationality() != null) {
            existingCharacter.setCharacterNationality(dto.characterNationality());
        }
        return characterRepo.save(existingCharacter);
    }

    @Override
    public void deleteCharacter(Long id) {
        characterRepo.findById(id).orElseThrow(()-> new CharacterNotFound(id));
        characterRepo.deleteById(id);

    }

    @Override
    public WorldCharacter transformFromDto(CharacterDto dto, Long worldId) {
        WorldCharacter character = new WorldCharacter();
        World world = worldRepo.findById(worldId).orElseThrow(() -> new WorldNotFound(worldId));
        character.setCharacterName(dto.characterName());
        character.setAge(dto.age());
        character.setCharacterSpecies(dto.characterSpecies());
        character.setCharacterNationality(dto.characterNationality());
        character.setCharacterDescription(dto.characterDescription());
        character.setWorld(world);

        return character;

    }
}
