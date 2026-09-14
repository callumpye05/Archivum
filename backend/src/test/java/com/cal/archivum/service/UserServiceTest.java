package com.cal.archivum.service;

import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.exception.EmailAlreadyUsed;
import com.cal.archivum.exception.UserNotFoundByEmailOrUsername;
import com.cal.archivum.exception.UsernameAlreadyUsed;
import com.cal.archivum.repository.UserRepository;
import com.cal.archivum.service.impl.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User testUser;


    @BeforeEach
    void setUp() {

        userService =
                new UserService(
                        userRepo,
                        passwordEncoder
                );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("test_user");
        testUser.setEmail("test@archivum.local");
        testUser.setUserHashedPassword("hashedPassword");
    }


    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }


    // =========================================================
    // CREATE USER
    // =========================================================

    @Test
    void createUser_shouldSaveUser_whenEmailAndUsernameAreAvailable() {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userRepo.existsByEmail(
                        "test@archivum.local"
                )
        )
                .thenReturn(false);

        when(
                userRepo.existsByUserName(
                        "test_user"
                )
        )
                .thenReturn(false);

        when(
                passwordEncoder.encode(
                        "password123"
                )
        )
                .thenReturn("encodedPassword");

        when(
                userRepo.save(
                        any(User.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        User result =
                userService.createUser(dto);


        assertEquals(
                "test_user",
                result.getUserName()
        );

        assertEquals(
                "test@archivum.local",
                result.getEmail()
        );

        assertEquals(
                "encodedPassword",
                result.getUserHashedPassword()
        );

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepo)
                .save(any(User.class));
    }


    @Test
    void createUser_shouldThrowEmailAlreadyUsed_whenEmailExists() {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userRepo.existsByEmail(
                        "test@archivum.local"
                )
        )
                .thenReturn(true);


        assertThrows(
                EmailAlreadyUsed.class,
                () -> userService.createUser(dto)
        );


        verify(
                userRepo,
                never()
        )
                .save(any(User.class));

        verifyNoInteractions(
                passwordEncoder
        );
    }


    @Test
    void createUser_shouldThrowUsernameAlreadyUsed_whenUsernameExists() {

        CreateUserDto dto =
                new CreateUserDto(
                        "test_user",
                        "test@archivum.local",
                        "password123"
                );

        when(
                userRepo.existsByEmail(
                        "test@archivum.local"
                )
        )
                .thenReturn(false);

        when(
                userRepo.existsByUserName(
                        "test_user"
                )
        )
                .thenReturn(true);


        assertThrows(
                UsernameAlreadyUsed.class,
                () -> userService.createUser(dto)
        );


        verify(
                userRepo,
                never()
        )
                .save(any(User.class));

        verifyNoInteractions(
                passwordEncoder
        );
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser_whenUserExists() {

        authenticateAs("test_user");

        when(
                userRepo.findByUserName(
                        "test_user"
                )
        )
                .thenReturn(
                        Optional.of(testUser)
                );


        User result =
                userService.getCurrentUser();


        assertEquals(
                testUser.getId(),
                result.getId()
        );

        assertEquals(
                "test_user",
                result.getUserName()
        );

        verify(userRepo)
                .findByUserName(
                        "test_user"
                );
    }


    @Test
    void getCurrentUser_shouldThrow_whenAuthenticatedUserDoesNotExist() {

        authenticateAs("missing_user");

        when(
                userRepo.findByUserName(
                        "missing_user"
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                UserNotFoundByEmailOrUsername.class,
                () -> userService.getCurrentUser()
        );

        verify(userRepo)
                .findByUserName(
                        "missing_user"
                );
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Test
    void updateUser_shouldUpdateEmailAndUsername_whenValuesAreAvailable() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        "updated_user",
                        "updated@archivum.local",
                        null
                );

        when(
                userRepo.existsByEmailAndIdNot(
                        "updated@archivum.local",
                        1L
                )
        )
                .thenReturn(false);

        when(
                userRepo.existsByUserNameAndIdNot(
                        "updated_user",
                        1L
                )
        )
                .thenReturn(false);

        when(
                userRepo.save(testUser)
        )
                .thenReturn(testUser);


        User result =
                userService.updateUser(dto);


        assertEquals(
                "updated_user",
                result.getUserName()
        );

        assertEquals(
                "updated@archivum.local",
                result.getEmail()
        );

        verify(userRepo)
                .save(testUser);

        verifyNoInteractions(
                passwordEncoder
        );
    }


    @Test
    void updateUser_shouldUpdateOnlyEmail_whenUsernameIsNull() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        null,
                        "updated@archivum.local",
                        null
                );

        when(
                userRepo.existsByEmailAndIdNot(
                        "updated@archivum.local",
                        1L
                )
        )
                .thenReturn(false);

        when(
                userRepo.save(testUser)
        )
                .thenReturn(testUser);


        User result =
                userService.updateUser(dto);


        assertEquals(
                "test_user",
                result.getUserName()
        );

        assertEquals(
                "updated@archivum.local",
                result.getEmail()
        );

        verify(
                userRepo,
                never()
        )
                .existsByUserNameAndIdNot(
                        anyString(),
                        anyLong()
                );
    }


    @Test
    void updateUser_shouldUpdateOnlyUsername_whenEmailIsNull() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        "updated_user",
                        null,
                        null
                );

        when(
                userRepo.existsByUserNameAndIdNot(
                        "updated_user",
                        1L
                )
        )
                .thenReturn(false);

        when(
                userRepo.save(testUser)
        )
                .thenReturn(testUser);


        User result =
                userService.updateUser(dto);


        assertEquals(
                "updated_user",
                result.getUserName()
        );

        assertEquals(
                "test@archivum.local",
                result.getEmail()
        );

        verify(
                userRepo,
                never()
        )
                .existsByEmailAndIdNot(
                        anyString(),
                        anyLong()
                );
    }


    @Test
    void updateUser_shouldEncodeAndUpdatePassword_whenPasswordIsProvided() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        null,
                        null,
                        "newPassword"
                );

        when(
                passwordEncoder.encode(
                        "newPassword"
                )
        )
                .thenReturn(
                        "newEncodedPassword"
                );

        when(
                userRepo.save(testUser)
        )
                .thenReturn(testUser);


        User result =
                userService.updateUser(dto);


        assertEquals(
                "newEncodedPassword",
                result.getUserHashedPassword()
        );

        verify(passwordEncoder)
                .encode("newPassword");

        verify(userRepo)
                .save(testUser);
    }


    @Test
    void updateUser_shouldNotEncodePassword_whenPasswordIsNull() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        null,
                        null,
                        null
                );

        when(
                userRepo.save(testUser)
        )
                .thenReturn(testUser);


        userService.updateUser(dto);


        verifyNoInteractions(
                passwordEncoder
        );
    }


    @Test
    void updateUser_shouldThrowEmailAlreadyUsed_whenEmailBelongsToAnotherUser() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        null,
                        "other@archivum.local",
                        null
                );

        when(
                userRepo.existsByEmailAndIdNot(
                        "other@archivum.local",
                        1L
                )
        )
                .thenReturn(true);


        assertThrows(
                EmailAlreadyUsed.class,
                () -> userService.updateUser(dto)
        );


        verify(
                userRepo,
                never()
        )
                .save(any(User.class));
    }


    @Test
    void updateUser_shouldThrowUsernameAlreadyUsed_whenUsernameBelongsToAnotherUser() {

        authenticateCurrentUser();

        UpdateUserDto dto =
                new UpdateUserDto(
                        "other_user",
                        null,
                        null
                );

        when(
                userRepo.existsByUserNameAndIdNot(
                        "other_user",
                        1L
                )
        )
                .thenReturn(true);


        assertThrows(
                UsernameAlreadyUsed.class,
                () -> userService.updateUser(dto)
        );


        verify(
                userRepo,
                never()
        )
                .save(any(User.class));
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    @Test
    void deleteUser_shouldDeleteAuthenticatedUser() {

        authenticateCurrentUser();


        userService.deleteUser();


        verify(userRepo)
                .delete(testUser);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private void authenticateCurrentUser() {

        authenticateAs("test_user");

        when(
                userRepo.findByUserName(
                        "test_user"
                )
        )
                .thenReturn(
                        Optional.of(testUser)
                );
    }


    private void authenticateAs(
            String username
    ) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password"
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}