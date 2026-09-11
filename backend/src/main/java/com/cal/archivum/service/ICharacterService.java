package com.cal.archivum.service;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.WorldCharacter;

import java.util.List;

public interface ICharacterService  {

    List<WorldCharacter> getAllCharactersFromWorld(Long worldId);
    WorldCharacter getCharacter(Long id);
    WorldCharacter createCharacter(CreateCharacterDto dto , Long worldId);
    WorldCharacter updateCharacter(Long id, UpdateCharacterDto dto);
    void deleteCharacter(Long id);
    WorldCharacter transformFromDto(CharacterDto dto , Long worldId);
    WorldCharacter getCharacterByWorldId(Long worldId , Long characterId);
}
