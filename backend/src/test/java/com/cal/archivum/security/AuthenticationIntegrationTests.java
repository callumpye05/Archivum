package com.cal.archivum.security;

import com.cal.archivum.entity.User;
import com.cal.archivum.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthenticationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    @ServiceConnection
    static MySQLContainer mysql =new MySQLContainer("mysql:8.4");

    @Test
    void verified_user_can_login() throws Exception {

        //given
        User user =new User();
        user.setUserName("verified_test_integration");
        user.setEmail("verified_test_integration@example.com");
        user.setUserHashedPassword(passwordEncoder.encode("TestPassword123!"));
        user.setVerifiedUser(true);
        userRepository.save(user);

        //when then
        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "verified_test_integration").param("password", "TestPassword123!")).andExpect(status().isOk());
    }


    @Test
    void unverified_user_cannot_login() throws Exception {

        //given
        User user = new User();
        user.setUserName("unverified_test2");
        user.setEmail("unverified_test2@example.com");
        user.setUserHashedPassword(passwordEncoder.encode("TestPassword123!"));
        user.setVerifiedUser(false);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "unverified_test2").param("password", "TestPassword123!")).andExpect(status().isUnauthorized());
    }


}