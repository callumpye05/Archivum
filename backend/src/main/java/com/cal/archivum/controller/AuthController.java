package com.cal.archivum.controller;


import com.cal.archivum.dto.EmailVerificationDto;
import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.EmailVerificationTokenDto;
import com.cal.archivum.dto.impl.UserResponseDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.service.IEmailVerificationTokenService;
import com.cal.archivum.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IUserService userService;
    private final IEmailVerificationTokenService emailVerificationTokenService;

    public AuthController(IUserService userService, IEmailVerificationTokenService emailVerificationTokenService) {
        this.userService =userService;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody CreateUserDto dto) {
        return userService.createUser(dto);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody EmailVerificationTokenDto dto){
        emailVerificationTokenService.verifyEmail(dto.token());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

}
