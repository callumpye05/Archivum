package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.enums.LocationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLocationDto(

        @NotNull
        @Size(max=100)
        String locationName,

        @NotNull
        LocationType locationType,

        @NotNull
        @Size(max=500)
        String locationDesc
) implements LocationDto {
}
