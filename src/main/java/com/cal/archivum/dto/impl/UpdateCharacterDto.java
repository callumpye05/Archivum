package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.CharacterDto;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateCharacterDto(
        @Size(max = 100)
        String characterName,

        @Size(max = 100)
        String characterSpecies,

        @PositiveOrZero
        Integer age,

        @Size(max = 500)
        String characterDescription,

        @Size(max = 100)
        String characterNationality) implements CharacterDto {
}