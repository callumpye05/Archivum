package com.cal.archivum.service;

public interface IEmailService {
    public void sendVerificationEmail(String receiver, String rawToken);
}


