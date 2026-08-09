package com.cal.archivum.service;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.dto.WorldDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;

import java.util.List;

public interface ICharacterService  {

    List<WorldCharacter> getAllCharactersFromWorld(Long worldId);
    WorldCharacter getCharacter(Long id);
    WorldCharacter createCharacter(CharacterDto dto , Long worldId);
    WorldCharacter updateCharacter(Long id, CharacterDto dto);
    void deleteCharacter(Long id);
    WorldCharacter transformFromDto(CharacterDto dto , Long worldId);

}
