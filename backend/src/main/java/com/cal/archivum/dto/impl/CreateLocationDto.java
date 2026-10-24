package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLocationDto(

        @NotBlank(message = "Location name cannot be null or empty")
        @Size(max=100 , message = "Max location size is 100")
        String locationName,

        @NotNull( message = "Location type cannot be null")
        LocationType locationType,

        @NotNull(message = "Location description cannot be null")
        @Size(max=500 , message = "Max size for the location description is 500")
        String locationDesc
) implements LocationDto {
}
