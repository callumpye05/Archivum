package com.cal.archivum.controller;

import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.security.ArchivumSecurityConfig;
import com.cal.archivum.service.IUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ArchivumSecurityConfig.class)
@WithMockUser(username = "test_user", roles = "USER")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    private User testUser;


    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("test_user");
        testUser.setEmail("test@archivum.local");
        testUser.setUserHashedPassword("{noop}password");
    }


    // =========================================================
    // SECURITY
    // =========================================================

    @Test
    @WithAnonymousUser
    void updateUser_shouldReturn401_whenUserIsNotAuthenticated()
            throws Exception {

        UpdateUserDto dto =
                new UpdateUserDto(
                        "updated_user",
                        "updated@archivum.local",
                        "newPassword"
                );

        mockMvc.perform(
                        put("/users/me")
                                .contentType("application/json")
                                .content(
                                        objectMapper
                                                .writeValueAsString(dto)
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }


    @Test
    @WithAnonymousUser
    void deleteUser_shouldReturn401_whenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        delete("/users/me")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Test
    void updateUser_shouldReturn200_whenRequestIsValid()
            throws Exception {

        UpdateUserDto dto =
                new UpdateUserDto(
                        "updated_user",
                        "updated@archivum.local",
                        "newPassword"
                );

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUserName("updated_user");
        updatedUser.setEmail("updated@archivum.local");
        updatedUser.setUserHashedPassword(
                "{noop}newPassword"
        );

        when(
                userService.updateUser(
                        any(UpdateUserDto.class)
                )
        )
                .thenReturn(updatedUser);

        mockMvc.perform(
                        put("/users/me")
                                .contentType("application/json")
                                .content(
                                        objectMapper
                                                .writeValueAsString(dto)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1L)
                )
                .andExpect(
                        jsonPath("$.userName")
                                .value("updated_user")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "updated@archivum.local"
                                )
                );

        verify(userService)
                .updateUser(
                        any(UpdateUserDto.class)
                );
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @Test
    void deleteUser_shouldReturn200_whenUserIsAuthenticated()
            throws Exception {

        doNothing()
                .when(userService)
                .deleteUser();

        mockMvc.perform(
                        delete("/users/me")
                )
                .andExpect(status().isOk());

        verify(userService)
                .deleteUser();
    }
}