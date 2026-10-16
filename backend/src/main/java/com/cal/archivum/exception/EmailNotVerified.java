package com.cal.archivum.exception;

import org.springframework.security.core.AuthenticationException;

public class EmailNotVerified extends AuthenticationException {
    public EmailNotVerified(String message) {
        super(message);
    }
}
