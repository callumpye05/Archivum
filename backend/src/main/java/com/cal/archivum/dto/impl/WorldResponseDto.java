package com.cal.archivum.dto.impl;

import jakarta.validation.constraints.Size;

public record WorldResponseDto(Long worldId , String worldName , String worldDesc) {
}
