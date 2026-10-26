package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.CharacterDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCharacterDto(

        @NotBlank(message = "Character name is required")
        @Size(max = 100, message = "Character name must not exceed 100 characters")
        String characterName,

        @NotBlank(message = "Character species is required")
        @Size(max = 100, message = "Character species must not exceed 100 characters")
        String characterSpecies,

        @NotNull(message = "Character age is required")
        @PositiveOrZero(message = "Character age cannot be negative")
        Integer age,

        @NotNull(message = "Character description is required")
        @Size(max = 500, message = "Character description must not exceed 500 characters")
        String characterDescription,

        @NotNull(message = "Character nationality is required")
        @Size(max = 100, message = "Character nationality must not exceed 100 characters")
        String characterNationality

) implements CharacterDto {}