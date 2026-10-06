package com.cal.archivum.exception;

public class EmailVerificationTokenExpired extends RuntimeException {
    public EmailVerificationTokenExpired() {

        super("This Email Verification Token Has Expired");
    }
}
