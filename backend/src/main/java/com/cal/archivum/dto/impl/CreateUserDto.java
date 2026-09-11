package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.UserDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @NotNull
        @Size(max = 100)
        String userName,

        @NotNull
        @Size(max = 254)
        String email,

        @NotNull
        @Size(min =  8, max = 100)
        String password
                              ) implements UserDto {


}
