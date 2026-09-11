package com.cal.archivum.dto;

import com.cal.archivum.enums.LocationType;

public interface LocationDto {


    String locationName();


    LocationType locationType();

    String locationDesc();
}
