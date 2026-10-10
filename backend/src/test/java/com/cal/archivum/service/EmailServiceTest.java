package com.cal.archivum.service;

import com.cal.archivum.service.impl.EmailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationEmail_shouldSendCorrectEmail() {

        String recipient ="testuser@example.com";
        String rawToken ="test-raw-token";

        emailService.sendVerificationEmail(recipient, rawToken);

        ArgumentCaptor<SimpleMailMessage> captor =ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sentMessage =captor.getValue();
        assertArrayEquals(new String[]{recipient}, sentMessage.getTo());
        assertEquals("noreply@archivum.local",sentMessage.getFrom());
        assertEquals("Verify your Archivum account", sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(rawToken));
        assertTrue(sentMessage.getText().contains("http://localhost:5173/verify-email?token=" + rawToken));
    }

    @Test
    void sendVerificationEmail_shouldPropagateMailFailure() {

        doThrow(new org.springframework.mail.MailSendException("SMTP unavailable")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(org.springframework.mail.MailSendException.class, () -> emailService.sendVerificationEmail("testuser@example.com", "test-raw-token"));
    }
}