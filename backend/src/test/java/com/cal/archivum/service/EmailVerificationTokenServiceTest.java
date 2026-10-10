package com.cal.archivum.service;

import com.cal.archivum.entity.EmailVerificationToken;
import com.cal.archivum.entity.User;
import com.cal.archivum.exception.EmailVerificationTokenExpired;
import com.cal.archivum.exception.EmailVerificationTokenNotFound;
import com.cal.archivum.repository.EmailVerificationTokenRepository;
import com.cal.archivum.service.impl.EmailVerificationTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationTokenServiceTest {

    @Mock
    private EmailVerificationTokenRepository emailRepo;

    @InjectMocks
    private EmailVerificationTokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser =new User();
        testUser.setVerifiedUser(false);
    }

    @Test
    void createToken_shouldPersistHashAndReturnRawToken() {

        String rawToken = tokenService.createEmailVerificationToken(testUser);

        ArgumentCaptor<EmailVerificationToken> captor =ArgumentCaptor.forClass(EmailVerificationToken.class); // since we don't return the token
        verify(emailRepo).save(captor.capture());
        EmailVerificationToken savedToken = captor.getValue();
        assertNotNull(rawToken);
        assertEquals(64, rawToken.length());
        assertNotNull(savedToken.getHashedToken());
        assertEquals(64, savedToken.getHashedToken().length());
        assertNotEquals(rawToken, savedToken.getHashedToken());
        assertSame(testUser, savedToken.getUser());
        Instant now = Instant.now();
        Instant expiry = savedToken.getExpirationDate();
        assertTrue(expiry.isAfter(now.plus(59, ChronoUnit.MINUTES)));
        assertTrue(expiry.isBefore(now.plus(61, ChronoUnit.MINUTES)));
    }

    @Test
    void verifyEmail_shouldVerifyUserAndDeleteToken_whenValid() {

        EmailVerificationToken token =new EmailVerificationToken(Instant.now().plus(1, ChronoUnit.HOURS), "stored-hash", testUser);
        when(emailRepo.findByHashedToken(anyString())).thenReturn(Optional.of(token));
        tokenService.verifyEmail("good-raw-token");
        assertTrue(testUser.isVerifiedUser());
        verify(emailRepo).delete(token);
    }

    @Test
    void verifyEmail_shouldThrowException_whenTokenExpired() {

        EmailVerificationToken expiredToken =new EmailVerificationToken(Instant.now().minus(1, ChronoUnit.HOURS), "stored-hash", testUser);

        when(emailRepo.findByHashedToken(anyString())).thenReturn(Optional.of(expiredToken));
        assertThrows(EmailVerificationTokenExpired.class, () -> tokenService.verifyEmail("expired-raw-token"));
        assertFalse(testUser.isVerifiedUser());
        verify(emailRepo, never()).delete(any(EmailVerificationToken.class));
    }
    @Test
    void verifyEmail_shouldThrowException_whenTokenNotFound() {

        when(emailRepo.findByHashedToken(anyString())).thenReturn(Optional.empty());
        assertThrows(EmailVerificationTokenNotFound.class, () ->tokenService.verifyEmail("unknown-raw-token"));
        assertFalse(testUser.isVerifiedUser());
        verify(emailRepo, never()).delete(any(EmailVerificationToken.class));
    }
}
