package com.cal.archivum.exception;

public class EmailVerificationTokenNotFound extends RuntimeException {
    public EmailVerificationTokenNotFound() {

        super("Email Verification Token not found");
    }
}
