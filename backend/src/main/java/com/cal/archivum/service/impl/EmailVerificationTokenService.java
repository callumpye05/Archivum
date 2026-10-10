package com.cal.archivum.service.impl;

import com.cal.archivum.entity.EmailVerificationToken;
import com.cal.archivum.entity.User;
import com.cal.archivum.exception.EmailVerificationTokenExpired;
import com.cal.archivum.exception.EmailVerificationTokenNotFound;
import com.cal.archivum.repository.EmailVerificationTokenRepository;
import com.cal.archivum.service.IEmailVerificationTokenService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class EmailVerificationTokenService implements IEmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailRepo;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationTokenService(EmailVerificationTokenRepository emailRepo) {
        this.emailRepo = emailRepo;
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest =MessageDigest.getInstance("SHA-256");
            byte[] hash =digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm isn't accessible right now",e);
        }
    }

    @Override
    public String createEmailVerificationToken(User user) {
        String rawToken = generateToken();
        String hashedToken = hashToken(rawToken);
        Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS);
        EmailVerificationToken token = new EmailVerificationToken(expiryDate, hashedToken, user);
        emailRepo.save(token);
        return rawToken;

    }
    @Transactional
    @Override
    public void verifyEmail(String rawToken) {
        String hashVersion = hashToken(rawToken);
        EmailVerificationToken token = emailRepo.findByHashedToken(hashVersion).orElseThrow(() -> new EmailVerificationTokenNotFound());

        if(token.getExpirationDate().isBefore(Instant.now())) {
            throw new EmailVerificationTokenExpired();
        }
            User user = token.getUser();
            user.setVerifiedUser(true);
            emailRepo.delete(token);
    }
}
