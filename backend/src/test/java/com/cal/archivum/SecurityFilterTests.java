package com.cal.archivum;

import com.cal.archivum.repository.CharacterRepository;
import com.cal.archivum.repository.LocationRepository;
import com.cal.archivum.repository.UserRepository;
import com.cal.archivum.repository.WorldRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityFilterTests {



        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private WorldRepository worldRepository;

        @Autowired
        private LocationRepository locationRepository;

        @Autowired
        private CharacterRepository characterRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;



        @Test
        void login_should_create_authenticated_session() throws Exception {
            MvcResult loginResult =mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_verified").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();

            MockHttpSession session= (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);
            mockMvc.perform(get("/users/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.userName").value("archivum_verified"));
        }


        @Test
        void login_shouldReturn401_whenPasswordIsIncorrect() throws Exception {

            mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_verified").param("password", "WrongPassword123!")).andExpect(status().isUnauthorized());
        }

        @Test
        void login_should_return_403_whenEmailIsntVerified() throws Exception {

            mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_test").param("password", "TestPassword123!")).andExpect(status().isUnauthorized());
        }



        @Test
        void login_shouldReturn403_whenCsrfIsMissing() throws Exception {

            mockMvc.perform(post("/auth/login").param("username", "archivum_test").param("password", "TestPassword123!")).andExpect(status().isForbidden());
        }

        @Test
        void getMe_shouldReturn401_whenUnauthenticated() throws Exception{
            mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
        }

        @Test
        void logout_shouldInvalidateAuthenticatedSession() throws Exception {
            MvcResult loginResult =mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_verified").param("password","TestPassword123!")).andExpect(status().isOk()).andReturn();
            MockHttpSession session= (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);
            mockMvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
            assertTrue(session.isInvalid());
        }

        @Test
        void getMe_shouldReturn401_afterLogout() throws Exception{
            MvcResult loginResult = mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_verified").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);

            mockMvc.perform(get("/users/me").session(session)).andExpect(status().isOk());

            mockMvc.perform(post("/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
            MockHttpSession staleSession =new MockHttpSession();
            mockMvc.perform(get("/users/me").session(staleSession)).andExpect(status().isUnauthorized());
            assertTrue(session.isInvalid());
        }




}
