package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.LocationDto;
import com.cal.archivum.enums.LocationType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateLocationDto(

        @Pattern(regexp= ".*\\S.*", message ="Location name cannot be blank")
        @Size(max = 100, message = "Location name must not exceed 100 characters")
        String locationName,

        LocationType locationType,

        @Size(max = 500, message = "Location description must not exceed 500 characters")
        String locationDesc

) implements LocationDto {}