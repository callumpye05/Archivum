package com.cal.archivum.service;

import com.cal.archivum.entity.EmailVerificationToken;
import com.cal.archivum.entity.User;

public interface IEmailVerificationTokenService {

    String createEmailVerificationToken(User user);

    void verifyEmail(String rawToken);
}
