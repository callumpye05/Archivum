package com.cal.archivum.service;

import com.cal.archivum.dto.UserDto;
import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.dto.impl.UserResponseDto;
import com.cal.archivum.entity.User;

public interface IUserService {

    UserResponseDto createUser(CreateUserDto createUser);
    UserResponseDto updateUser(UpdateUserDto UpdateUser);
    void deleteUser();
    User getCurrentUser();
}