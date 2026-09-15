package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.UserResponseDto;
import com.cal.archivum.exception.EmailAlreadyUsed;
import com.cal.archivum.exception.UsernameAlreadyUsed;
import com.cal.archivum.security.ArchivumSecurityConfig;
import com.cal.archivum.service.IUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(ArchivumSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    private UserResponseDto testUserResponse;


    @BeforeEach
    void setUp() {

        testUserResponse =
                new UserResponseDto(
                        "test_user",
                        "test@archivum.local"
                );
    }


    @Test
    void register_shouldReturn200_whenRequestIsValid()
            throws Exception {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userService.createUser(
                        any(CreateUserDto.class)
                )
        )
                .thenReturn(testUserResponse);


        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(dto)
                                )
                )
                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.userName")
                                .value("test_user")
                )

                .andExpect(
                        jsonPath("$.email")
                                .value("test@archivum.local")
                )

                .andExpect(
                        jsonPath("$.userHashedPassword")
                                .doesNotExist()
                );


        verify(userService)
                .createUser(
                        any(CreateUserDto.class)
                );
    }


    @Test
    void register_shouldAllowAnonymousUser()
            throws Exception {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userService.createUser(
                        any(CreateUserDto.class)
                )
        )
                .thenReturn(testUserResponse);


        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(dto)
                                )
                )
                .andExpect(status().isOk());


        verify(userService)
                .createUser(
                        any(CreateUserDto.class)
                );
    }


    @Test
    void register_shouldReturnConflict_whenEmailAlreadyExists()
            throws Exception {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userService.createUser(
                        any(CreateUserDto.class)
                )
        )
                .thenThrow(
                        new EmailAlreadyUsed(
                                "This email is used"
                        )
                );


        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(dto)
                                )
                )
                .andExpect(status().isConflict());


        verify(userService)
                .createUser(
                        any(CreateUserDto.class)
                );
    }


    @Test
    void register_shouldReturnConflict_whenUsernameAlreadyExists()
            throws Exception {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userService.createUser(
                        any(CreateUserDto.class)
                )
        )
                .thenThrow(
                        new UsernameAlreadyUsed(
                                "Username is already used"
                        )
                );


        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(dto)
                                )
                )
                .andExpect(status().isConflict());


        verify(userService)
                .createUser(
                        any(CreateUserDto.class)
                );
    }
}