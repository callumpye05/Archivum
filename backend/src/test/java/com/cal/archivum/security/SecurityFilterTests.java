package com.cal.archivum.security;

import com.cal.archivum.entity.User;
import com.cal.archivum.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class SecurityFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    @ServiceConnection
    static MySQLContainer mysql=new MySQLContainer("mysql:8.4");


    private void createUser(String username, boolean verif) {
        User user=new User();
        user.setUserName(username);
        user.setEmail(username +"@example.com");
        user.setUserHashedPassword(passwordEncoder.encode("TestPassword123!"));
        user.setVerifiedUser(verif);
        userRepository.save(user);
    }


    @Test
    void login_should_create_authenticated_session() throws Exception {
        //given
        createUser("session_creation_user", true);
        MvcResult loginResult =mockMvc.perform(post("/auth/login").with(csrf()).param("username", "session_creation_user").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/users/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.userName").value("session_creation_user"));
    }




    @Test
    void login_shouldReturn403_whenCsrfIsMissing() throws Exception {
        mockMvc.perform(post("/auth/login").param("username", "csrf_test_user").param("password", "TestPassword123!")).andExpect(status().isForbidden());
    }


    @Test
    void getMe_shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldInvalidateAuthenticatedSession() throws Exception {

        //given
        createUser("session_invalidation_user", true);
        MvcResult loginResult = mockMvc.perform(post("/auth/login").with(csrf()).param("username", "session_invalidation_user").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();
        MockHttpSession session= (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
    }

    @Test
    void logout_shouldnt_invalidateAuthenticatedSession_without_csrf() throws Exception {

        //given
        createUser("session_invalidation_user_without_csrf", true);
        MvcResult loginResult = mockMvc.perform(post("/auth/login").with(csrf()).param("username", "session_invalidation_user_without_csrf").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();
        MockHttpSession session= (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(post("/auth/logout").session(session)).andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
    }



    @Test
    void getMe_shouldReturn401_afterLogout() throws Exception {
        //given
        createUser("post_logout_user",true);
        MvcResult loginResult= mockMvc.perform(post("/auth/login").with(csrf()).param("username", "post_logout_user").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();
        MockHttpSession session=(MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/users/me").session(session)).andExpect(status().isOk());
        mockMvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
        mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
    }

}