package com.cal.archivum.service;

import com.cal.archivum.dto.CharacterDto;
import com.cal.archivum.dto.impl.CharacterResponseDto;
import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.World;
import com.cal.archivum.entity.WorldCharacter;

import java.util.List;

public interface ICharacterService  {

    List<CharacterResponseDto> getAllCharactersFromWorld(Long worldId);
    CharacterResponseDto getCharacter(Long id);
    CharacterResponseDto createCharacter(CreateCharacterDto dto , Long worldId);
    CharacterResponseDto updateCharacter(Long id, UpdateCharacterDto dto);
    void deleteCharacter(Long id);
    WorldCharacter transformFromDto(CharacterDto dto , World world);
    CharacterResponseDto getCharacterByWorldId(Long worldId , Long characterId);
    CharacterResponseDto toDto(WorldCharacter character);
}
