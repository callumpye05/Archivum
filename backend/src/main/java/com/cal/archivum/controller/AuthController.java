package com.cal.archivum.controller;


import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IUserService userService;

    public AuthController(IUserService userService) {
        this.userService =userService;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody CreateUserDto dto) {
        return userService.createUser(dto);
    }
}
