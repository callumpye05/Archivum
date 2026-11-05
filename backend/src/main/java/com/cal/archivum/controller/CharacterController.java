package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CharacterResponseDto;
import com.cal.archivum.dto.impl.CreateCharacterDto;
import com.cal.archivum.dto.impl.UpdateCharacterDto;
import com.cal.archivum.entity.WorldCharacter;
import com.cal.archivum.service.ICharacterService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CharacterController {

    private final ICharacterService characterService;

    public CharacterController(ICharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/worlds/{worldId}/characters")
    public List<CharacterResponseDto> getAllCharactersFromWorld(@PathVariable Long worldId) {
        return  characterService.getAllCharactersFromWorld(worldId);

    }

    @GetMapping("/characters/{characterId}")
    public CharacterResponseDto getCharacter(@PathVariable Long characterId) {
        return characterService.getCharacter(characterId);
    }


    @GetMapping("/worlds/{worldId}/characters/{characterId}")
    public CharacterResponseDto getCharacterByWorld(@PathVariable Long  worldId , @PathVariable Long characterId) {
        return characterService.getCharacterByWorldId(worldId , characterId);
    }


    @PostMapping ("/worlds/{worldId}/characters")
    public CharacterResponseDto createCharacter(@PathVariable Long worldId, @Valid @RequestBody CreateCharacterDto dto) {
        return characterService.createCharacter(dto, worldId);
    }

    @PutMapping("/characters/{characterId}")
    public CharacterResponseDto updateCharacter(@PathVariable Long characterId,  @Valid @RequestBody UpdateCharacterDto dto) {
        return characterService.updateCharacter(characterId , dto);
    }

    @DeleteMapping("/characters/{characterId}")
    public void deleteCharacter(@PathVariable Long characterId) {
        characterService.deleteCharacter(characterId);
    }

}
