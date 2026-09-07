package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.WorldDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorldDto (
        @NotNull
        @Size(max = 100)
        String worldName ,

        @NotNull
        @Size(max = 500)
        String worldDesc
)  implements WorldDto {
}
