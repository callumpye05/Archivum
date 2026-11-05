package com.cal.archivum.dto.impl;

import com.cal.archivum.enums.LocationType;

public record LocationResponseDto(Long locationId , String locationName , LocationType locationType , String locationDesc,
                                  Long worldId) {
}
