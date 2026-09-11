package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.enums.LocationType;
import jakarta.validation.constraints.Size;

public record UpdateLocationDto(

        @Size(max = 100)
        String locationName,

        LocationType locationType,

        @Size(max = 500)
        String locationDesc) implements LocationDto {
}
