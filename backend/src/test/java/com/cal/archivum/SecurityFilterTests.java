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
            MvcResult loginResult =mockMvc.perform(post("/auth/login").with(csrf()).param("username", "archivum_test").param("password", "TestPassword123!")).andExpect(status().isOk()).andReturn();

            MockHttpSession session= (MockHttpSession) loginResult.getRequest().getSession(false);
            assertNotNull(session);
            mockMvc.perform(get("/users/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.userName").value("archivum_test"));
        }



}
