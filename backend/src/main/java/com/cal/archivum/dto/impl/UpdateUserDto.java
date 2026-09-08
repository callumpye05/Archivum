package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.UserDto;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(

        @Size(max = 100)
        String userName,

        @Size(max = 254)
        String email,

        @Size(min = 8 , max = 100)
        String password
) implements UserDto {
}
