package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.WorldDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorldDto(

        @NotBlank(message = "World name is required")
        @Size(max = 100, message = "World name must not exceed 100 characters")
        String worldName,

        @NotNull(message = "World description is required")
        @Size(max = 500, message = "World description must not exceed 500 characters")
        String worldDesc

) implements WorldDto {}