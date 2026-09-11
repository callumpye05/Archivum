package com.cal.archivum.controller;


import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public User updateUser(@Valid @RequestBody UpdateUserDto userDto) {
        return userService.updateUser(userDto);
    }

    @DeleteMapping("/me")
    public void deleteUser() {
         userService.deleteUser();
    }
 }
