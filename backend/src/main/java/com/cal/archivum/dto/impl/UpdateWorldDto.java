package com.cal.archivum.dto.impl;


import com.cal.archivum.dto.WorldDto;
import jakarta.validation.constraints.Size;

public record UpdateWorldDto(
        @Size(max = 100)
        String worldName ,

        @Size(max = 500)
        String worldDesc) implements WorldDto {
}
