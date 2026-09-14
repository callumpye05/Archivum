package com.cal.archivum.repository;

import com.cal.archivum.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setUserName("test_user");
        testUser.setEmail("test@archivum.local");
        testUser.setUserHashedPassword("{noop}password");

        testUser = userRepository.save(testUser);


        otherUser = new User();
        otherUser.setUserName("other_user");
        otherUser.setEmail("other@archivum.local");
        otherUser.setUserHashedPassword("{noop}password");

        otherUser = userRepository.save(otherUser);
    }


    // =========================================================
    // FIND BY USERNAME
    // =========================================================

    @Test
    void findByUserName_shouldReturnUser_whenUsernameExists() {

        Optional<User> result =
                userRepository.findByUserName("test_user");

        assertTrue(result.isPresent());

        assertEquals(
                testUser.getId(),
                result.get().getId()
        );
    }


    @Test
    void findByUserName_shouldReturnEmpty_whenUsernameDoesNotExist() {

        Optional<User> result =
                userRepository.findByUserName("missing_user");

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND BY EMAIL
    // =========================================================

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {

        Optional<User> result =
                userRepository.findByEmail(
                        "test@archivum.local"
                );

        assertTrue(result.isPresent());

        assertEquals(
                testUser.getId(),
                result.get().getId()
        );
    }


    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        Optional<User> result =
                userRepository.findByEmail(
                        "missing@archivum.local"
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND BY USERNAME OR EMAIL
    // =========================================================

    @Test
    void findByUserNameOrEmail_shouldReturnUser_whenUsernameMatches() {

        Optional<User> result =
                userRepository.findByUserNameOrEmail(
                        "test_user",
                        "wrong@archivum.local"
                );

        assertTrue(result.isPresent());

        assertEquals(
                testUser.getId(),
                result.get().getId()
        );
    }


    @Test
    void findByUserNameOrEmail_shouldReturnUser_whenEmailMatches() {

        Optional<User> result =
                userRepository.findByUserNameOrEmail(
                        "wrong_user",
                        "test@archivum.local"
                );

        assertTrue(result.isPresent());

        assertEquals(
                testUser.getId(),
                result.get().getId()
        );
    }


    @Test
    void findByUserNameOrEmail_shouldReturnEmpty_whenNeitherMatches() {

        Optional<User> result =
                userRepository.findByUserNameOrEmail(
                        "missing_user",
                        "missing@archivum.local"
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // EXISTS BY USERNAME
    // =========================================================

    @Test
    void existsByUserName_shouldReturnTrue_whenUsernameExists() {

        boolean result =
                userRepository.existsByUserName(
                        "test_user"
                );

        assertTrue(result);
    }


    @Test
    void existsByUserName_shouldReturnFalse_whenUsernameDoesNotExist() {

        boolean result =
                userRepository.existsByUserName(
                        "missing_user"
                );

        assertFalse(result);
    }


    // =========================================================
    // EXISTS BY EMAIL
    // =========================================================

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {

        boolean result =
                userRepository.existsByEmail(
                        "test@archivum.local"
                );

        assertTrue(result);
    }


    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {

        boolean result =
                userRepository.existsByEmail(
                        "missing@archivum.local"
                );

        assertFalse(result);
    }


    // =========================================================
    // EXISTS BY EMAIL AND ID NOT
    // =========================================================

    @Test
    void existsByEmailAndIdNot_shouldReturnTrue_whenEmailBelongsToDifferentUser() {

        boolean result =
                userRepository.existsByEmailAndIdNot(
                        "other@archivum.local",
                        testUser.getId()
                );

        assertTrue(result);
    }


    @Test
    void existsByEmailAndIdNot_shouldReturnFalse_whenEmailBelongsToSameUser() {

        boolean result =
                userRepository.existsByEmailAndIdNot(
                        "test@archivum.local",
                        testUser.getId()
                );

        assertFalse(result);
    }


    @Test
    void existsByEmailAndIdNot_shouldReturnFalse_whenEmailDoesNotExist() {

        boolean result =
                userRepository.existsByEmailAndIdNot(
                        "missing@archivum.local",
                        testUser.getId()
                );

        assertFalse(result);
    }


    // =========================================================
    // EXISTS BY USERNAME AND ID NOT
    // =========================================================

    @Test
    void existsByUserNameAndIdNot_shouldReturnTrue_whenUsernameBelongsToDifferentUser() {

        boolean result =
                userRepository.existsByUserNameAndIdNot(
                        "other_user",
                        testUser.getId()
                );

        assertTrue(result);
    }


    @Test
    void existsByUserNameAndIdNot_shouldReturnFalse_whenUsernameBelongsToSameUser() {

        boolean result =
                userRepository.existsByUserNameAndIdNot(
                        "test_user",
                        testUser.getId()
                );

        assertFalse(result);
    }


    @Test
    void existsByUserNameAndIdNot_shouldReturnFalse_whenUsernameDoesNotExist() {

        boolean result =
                userRepository.existsByUserNameAndIdNot(
                        "missing_user",
                        testUser.getId()
                );

        assertFalse(result);
    }
}