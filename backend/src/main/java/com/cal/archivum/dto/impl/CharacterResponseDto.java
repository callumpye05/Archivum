package com.cal.archivum.dto.impl;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CharacterResponseDto(Long characterId, String characterName, String characterSpecies, Integer age,
                                   String characterDescription,
                                   String characterNationality,
                                   Long worldId
                                   ) {
}
