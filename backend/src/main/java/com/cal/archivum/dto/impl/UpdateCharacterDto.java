package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.CharacterDto;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateCharacterDto(

        @Size(max = 100, message ="Character name must not exceed 100 characters")
        String characterName,

        @Size(max = 100, message = "Character species must not exceed 100 characters")
        String characterSpecies,

        @PositiveOrZero(message= "Character age cannot be negative")
        Integer age,

        @Size(max =500, message ="Character description must not exceed 500 characters")
        String characterDescription,

        @Size(max = 100, message = "Character nationality must not exceed 100 characters")
        String characterNationality

) implements CharacterDto {}