package com.cal.archivum.service.impl;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.exception.CharacterNotFound;
import com.cal.archivum.exception.CharacterNotFoundByWorld;
import com.cal.archivum.exception.WorldNotFound;
import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.WorldRepository;
import com.cal.archivum.service.ICharacterService;
import com.cal.archivum.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharacterService implements ICharacterService {

    private final CharacterRepository characterRepo;
    private final WorldRepository worldRepo;
    private final IUserService userService;

    public CharacterService(CharacterRepository characterRepo, WorldRepository worldRepo, IUserService userService) {
        this.characterRepo = characterRepo;
        this.worldRepo = worldRepo;
        this.userService = userService;
    }

    @Override
    public List<WorldCharacter> getAllCharactersFromWorld(Long worldId) {
        User currentUser = userService.getCurrentUser();
        World world = worldRepo.findByWorldIdAndOwner(worldId , currentUser).orElseThrow(() -> new WorldNotFound(worldId));
        return characterRepo.findAllByWorld(world);
    }

    @Override
    public WorldCharacter getCharacter(Long id) {
       User currentUser = userService.getCurrentUser();
       return characterRepo.findByCharacterIdAndWorldOwner(id , currentUser).orElseThrow(() -> new CharacterNotFound(id));
    }

    @Override
    public WorldCharacter getCharacterByWorldId(Long worldId, Long characterId) {
        User currentUser = userService.getCurrentUser();
        return characterRepo.findByCharacterIdAndWorldWorldIdAndWorldOwner(characterId, worldId , currentUser).orElseThrow(() -> new CharacterNotFoundByWorld(characterId , worldId));
    }



    @Override
    public WorldCharacter createCharacter(CreateCharacterDto dto , Long worldId) {
        User currentUser = userService.getCurrentUser();
        World world = worldRepo.findByWorldIdAndOwner(worldId , currentUser).orElseThrow(()-> new WorldNotFound(worldId));
        WorldCharacter character = transformFromDto(dto,world);
        return characterRepo.save(character);
    }

    @Override
    public WorldCharacter updateCharacter(Long id, UpdateCharacterDto dto) {
        User currentUser = userService.getCurrentUser();
        WorldCharacter existingCharacter = characterRepo.findByCharacterIdAndWorldOwner(id , currentUser).orElseThrow(()-> new CharacterNotFound(id));

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
        User currentUser = userService.getCurrentUser();

        WorldCharacter character = characterRepo.findByCharacterIdAndWorldOwner(id, currentUser).orElseThrow(() -> new CharacterNotFound(id));
        characterRepo.delete(character);
    }

    @Override
    public WorldCharacter transformFromDto(CharacterDto dto, World world) {
        WorldCharacter character = new WorldCharacter();
        character.setCharacterName(dto.characterName());
        character.setAge(dto.age());
        character.setCharacterSpecies(dto.characterSpecies());
        character.setCharacterNationality(dto.characterNationality());
        character.setCharacterDescription(dto.characterDescription());
        character.setWorld(world);

        return character;

    }
}
