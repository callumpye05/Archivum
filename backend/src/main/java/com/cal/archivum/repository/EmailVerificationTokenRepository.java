package com.cal.archivum.repository;

import com.cal.archivum.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken , Long> {

    Optional<EmailVerificationToken> findByHashedToken(String hashedToken);
}
