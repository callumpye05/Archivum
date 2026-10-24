package com.cal.archivum.dto.impl;

import com.cal.archivum.dto.UserDto;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;


public record UpdateUserDto(

        @Pattern(regexp = ".*\\S.*",message = "Username cannot be blank")
        @Size(max = 100, message = "Username must not exceed 100 characters")
        String userName,

        @Email(message= "Please enter a valid email address")
        @Pattern(regexp = ".*\\S.*", message = "Email address cannot be blank")
        @Size(max = 254, message = "Email address must not exceed 254 characters")
        String email,

        @Pattern(regexp =".*\\S.*", message = "Password cannot be blank")
        @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
        String password

) implements UserDto {}