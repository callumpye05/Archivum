package com.cal.archivum.dto.impl;


import com.cal.archivum.dto.WorldDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWorldDto(

        @Pattern(regexp = ".*\\S.*",message = "World name cannot be blank")
        @Size(max = 100, message = "World name must not exceed 100 characters")
        String worldName ,

        @Size(max = 500 , message = "World description size cannot exceed 500 characters")
        String worldDesc) implements WorldDto {
}
