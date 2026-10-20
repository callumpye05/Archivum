package com.cal.archivum.security;

import com.cal.archivum.entity.User;
import com.cal.archivum.repository.UserRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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


    private void createUser(String username, boolean verif){
        User user =new User();
        user.setUserName(username);
        user.setEmail(username +"@example.com");
        user.setUserHashedPassword(passwordEncoder.encode("TestPassword123!"));
        user.setVerifiedUser(verif);

        userRepository.save(user);
    }

    @Test
    void verified_user_can_login() throws Exception {
        // Given
        createUser("verified_login_success", true);
        //When /then
        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "verified_login_success").param("password", "TestPassword123!")).andExpect(status().isOk());
    }

    @Test
    void unverified_user_cannot_login() throws Exception {
        // Given
        createUser("unverified_login", false);
        //When /then
        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "unverified_login").param("password", "TestPassword123!")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
    }

    @Test
    void verified_user_cannot_login_with_wrong_password() throws Exception {
        // Given
        createUser("verified_wrong_password", true);
        //When /then
        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "verified_wrong_password").param("password", "TestPassword125555!")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
    @Test
    void unverified_user_cannot_login_with_wrong_password() throws Exception {
        // Given
        createUser("unverified_wrong_password", false);

        //When /then
        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "unverified_wrong_password").param("password", "TestPassword125555!")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void verified_user_can_login_with_email() throws Exception {
        //given
        createUser("email_login_user", true);
        //when/then

        mockMvc.perform(post("/auth/login").with(csrf()).param("username", "email_login_user@example.com").param("password", "TestPassword123!")).andExpect(status().isOk());
    }


    @Test
    void cannot_login_with_nonexistant_username() throws Exception {
        mockMvc.perform(post("/auth/login").with(csrf()).param("username" , "fake_account@example.com").param("password", "FakePassword123!")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

}