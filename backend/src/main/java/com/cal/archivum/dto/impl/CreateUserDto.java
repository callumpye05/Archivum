package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.UserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(

        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username must not exceed 100 characters")
        String userName,

        @NotBlank(message = "Email address is required")
        @Email(message = "Please enter a valid email address")
        @Size(max = 254, message = "Email address must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
        String password

) implements UserDto {}