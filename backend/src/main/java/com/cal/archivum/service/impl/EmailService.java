package com.cal.archivum.service.impl;

import com.cal.archivum.service.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService implements IEmailService {

    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String receiver, String rawToken) throws MailSendException {

        String verificationUrl ="http://localhost:5173/verify-email?token=" +rawToken;
        SimpleMailMessage message =new SimpleMailMessage();
        message.setFrom("noreply@archivum.local");
        message.setTo(receiver);
        message.setSubject("Verify your Archivum account");
        message.setText("Welcome to Archivum!\n\n" +"Verify your email using the following link:\n" + verificationUrl + "\n\n" + "This link expires in one hour.");
        mailSender.send(message);
    }

}
