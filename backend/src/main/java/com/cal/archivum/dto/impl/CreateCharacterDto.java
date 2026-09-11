package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.CharacterDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCharacterDto(
        @NotNull
        @Size(max = 100)
        String characterName,

        @NotNull
        @Size(max = 100)
        String characterSpecies,

        @NotNull
        @PositiveOrZero
        Integer age,

        @NotNull
        @Size(max = 500)
        String characterDescription,

        @NotNull
        @Size(max = 100)
        String characterNationality) implements CharacterDto
 {
}
